//! Provider-independent primitives for Vybetube.
//! Network extraction and provider-specific playback are deliberately kept outside this crate.

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum NetworkClass {
    Offline,
    Metered,
    Unmetered,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum DownloadState {
    Queued,
    Running,
    Paused,
    Completed,
    Failed,
    Cancelled,
}

impl DownloadState {
    /// Returns whether a requested state transition is valid.
    pub fn can_transition_to(self, next: Self) -> bool {
        use DownloadState::*;
        matches!(
            (self, next),
            (Queued, Running | Cancelled)
                | (Running, Paused | Completed | Failed | Cancelled)
                | (Paused, Running | Cancelled)
                | (Failed, Queued | Cancelled)
        )
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub struct CachePolicy {
    pub max_bytes: u64,
    pub preload_next: bool,
    pub allow_metered_preload: bool,
}

impl CachePolicy {
    pub fn should_preload(&self, network: NetworkClass) -> bool {
        self.preload_next
            && match network {
                NetworkClass::Offline => false,
                NetworkClass::Metered => self.allow_metered_preload,
                NetworkClass::Unmetered => true,
            }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MediaIdentity {
    pub provider: String,
    pub content_id: String,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn completed_download_is_terminal() {
        assert!(!DownloadState::Completed.can_transition_to(DownloadState::Running));
    }

    #[test]
    fn failed_download_can_be_retried() {
        assert!(DownloadState::Failed.can_transition_to(DownloadState::Queued));
    }

    #[test]
    fn metered_preload_respects_policy() {
        let policy = CachePolicy {
            max_bytes: 256 * 1024 * 1024,
            preload_next: true,
            allow_metered_preload: false,
        };
        assert!(!policy.should_preload(NetworkClass::Metered));
        assert!(policy.should_preload(NetworkClass::Unmetered));
    }
}
