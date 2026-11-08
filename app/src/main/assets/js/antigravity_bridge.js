/**
 * Antigravity Native Android Mobile Bridge
 * Enables bidirectional communication between Antigravity Web surfaces (antigravity.google.com)
 * and the native Android OS (Heads-up notifications, Biometrics, Instance Switcher, Tool Approvals, Voice Mode).
 */
(function() {
    if (window.__antigravity_bridge_initialized) return;
    window.__antigravity_bridge_initialized = true;

    console.log('[Antigravity Mobile] Initializing native bridge...');

    // 1. Intercept Standard Web Notification API to trigger rich Android notifications
    const OriginalNotification = window.Notification;
    window.Notification = function(title, options = {}) {
        console.log('[Antigravity Mobile] Intercepted Web Notification:', title, options);
        if (window.AntigravityNative && window.AntigravityNative.postNotification) {
            window.AntigravityNative.postNotification(
                title || 'Antigravity Update',
                options.body || '',
                options.tag || 'general',
                options.data?.url || window.location.href
            );
        }
        if (OriginalNotification && OriginalNotification.permission === 'granted') {
            try { return new OriginalNotification(title, options); } catch (e) {}
        }
        return { close: () => {} };
    };
    window.Notification.permission = 'granted';
    window.Notification.requestPermission = async () => 'granted';

    // 2. DOM Mutation Observer to detect Tool Approvals, Clarifying Questions & Artifacts
    function setupDomObservers() {
        const observer = new MutationObserver((mutations) => {
            // Check for tool approval prompts (e.g., Run command confirmation dialog)
            const approvalModal = document.querySelector('[data-testid="tool-approval-dialog"], .tool-confirmation-card, [role="alertdialog"]');
            if (approvalModal && !approvalModal.dataset.nativeNotified) {
                approvalModal.dataset.nativeNotified = 'true';
                const commandText = approvalModal.querySelector('code, pre, .command-preview')?.textContent || 'Execution requested';
                const toolName = approvalModal.querySelector('.tool-name, h3')?.textContent || 'Terminal Tool';
                
                if (window.AntigravityNative && window.AntigravityNative.onApprovalRequested) {
                    window.AntigravityNative.onApprovalRequested(toolName, commandText, Date.now().toString());
                }
            }

            // Check for User Clarifying Questions (e.g. ask_question modal)
            const questionModal = document.querySelector('[data-testid="clarifying-question-modal"], .question-prompt-card');
            if (questionModal && !questionModal.dataset.nativeNotified) {
                questionModal.dataset.nativeNotified = 'true';
                const questionText = questionModal.querySelector('h3, .question-title')?.textContent || 'Agent needs clarification';
                if (window.AntigravityNative && window.AntigravityNative.postNotification) {
                    window.AntigravityNative.postNotification(
                        'Antigravity Needs Input',
                        questionText,
                        'question',
                        window.location.href
                    );
                }
            }

            // Check for newly generated Artifacts
            const artifactCards = document.querySelectorAll('[data-testid="artifact-card"]:not([data-native-seen])');
            artifactCards.forEach(card => {
                card.dataset.nativeSeen = 'true';
                const title = card.querySelector('h3, .artifact-title')?.textContent || 'New Artifact';
                const type = card.getAttribute('data-artifact-type') || 'document';
                const link = card.querySelector('a')?.href || window.location.href;
                if (window.AntigravityNative && window.AntigravityNative.onArtifactReady) {
                    window.AntigravityNative.onArtifactReady(title, link, type);
                }
            });

            // Check for Agent Status (Thinking / Executing / Idle)
            const statusEl = document.querySelector('.agent-status-indicator, [data-agent-state]');
            if (statusEl) {
                const state = statusEl.getAttribute('data-agent-state') || statusEl.textContent.trim();
                if (window.__last_agent_state !== state) {
                    window.__last_agent_state = state;
                    if (window.AntigravityNative && window.AntigravityNative.onStatusChanged) {
                        window.AntigravityNative.onStatusChanged(state, '');
                    }
                }
            }
        });

        observer.observe(document.body, { childList: true, subtree: true, attributes: true });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setupDomObservers);
    } else {
        setupDomObservers();
    }

    // 3. Exposed JavaScript hooks for Native Android callbacks
    window.__antigravity_approve = function(toolId, isApproved) {
        console.log('[Antigravity Mobile] Received native approval callback:', toolId, isApproved);
        const confirmBtn = document.querySelector('button[data-action="approve"], button[data-testid="confirm-approval"], button.confirm-btn');
        const rejectBtn = document.querySelector('button[data-action="reject"], button[data-testid="cancel-approval"], button.reject-btn');
        if (isApproved && confirmBtn) {
            confirmBtn.click();
        } else if (!isApproved && rejectBtn) {
            rejectBtn.click();
        }
    };

    window.__antigravity_send_prompt = function(promptText) {
        console.log('[Antigravity Mobile] Sending prompt from native bar:', promptText);
        const inputArea = document.querySelector('textarea, [contenteditable="true"], input[type="text"].chat-input');
        if (inputArea) {
            inputArea.focus();
            if (inputArea.tagName === 'TEXTAREA' || inputArea.tagName === 'INPUT') {
                inputArea.value = promptText;
                inputArea.dispatchEvent(new Event('input', { bubbles: true }));
            } else {
                inputArea.textContent = promptText;
            }
            const sendBtn = document.querySelector('button[data-testid="send-prompt"], button.send-btn, button[aria-label="Send"]');
            if (sendBtn) {
                setTimeout(() => sendBtn.click(), 100);
            }
        }
    };

    // Voice Mode Trigger (Gemini Live)
    window.__antigravity_toggle_voice = function() {
        console.log('[Antigravity Mobile] Toggling Gemini Live Voice Mode');
        const voiceBtn = document.querySelector('button[data-testid="voice-mode-toggle"], button[aria-label="Voice mode"], .voice-mic-button');
        if (voiceBtn) {
            voiceBtn.click();
        } else {
            window.__antigravity_send_prompt('/voice');
        }
    };

    // 4. Mobile layout styling tweaks (clean edge-to-edge look)
    const mobileStyle = document.createElement('style');
    mobileStyle.textContent = `
        /* Optimize touch targets and hide redundant desktop scrollbars */
        ::-webkit-scrollbar { width: 4px; height: 4px; }
        ::-webkit-scrollbar-thumb { background: rgba(124, 77, 255, 0.4); border-radius: 4px; }
        body { -webkit-tap-highlight-color: transparent; }
    `;
    document.head.appendChild(mobileStyle);

    console.log('[Antigravity Mobile] Bridge active with Voice Mode & Question observers.');
})();
