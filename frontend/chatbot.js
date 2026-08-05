/* ==========================================================================
   Payment Assistant — Rule-based chatbot
   No external AI API required. All intelligence is keyword/pattern matching
   wired to your existing backend endpoints.
========================================================================== */
(function () {
    'use strict';

    var API_BASE = (window.PAYMENT_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
    var ANALYTICS = API_BASE + '/analytics';
    var PAYMENTS  = API_BASE + '/payments';
    var HISTORY   = API_BASE + '/payment-history';

    /* ------------------------------------------------------------------
       STYLES (inherits CSS variables already on the page)
    ------------------------------------------------------------------ */
    var styleEl = document.createElement('style');
    styleEl.textContent = [
        '@keyframes chatSlideUp{from{opacity:0;transform:translateY(20px) scale(0.97)}to{opacity:1;transform:translateY(0) scale(1)}}',
        '@keyframes chatFabPop{0%{transform:scale(1)}50%{transform:scale(1.12)}100%{transform:scale(1)}}',
        '@keyframes typingBounce{0%,80%,100%{transform:translateY(0)}40%{transform:translateY(-6px)}}',
        '@keyframes chatMsgIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:translateY(0)}}',

        '.chat-fab{position:fixed;bottom:28px;right:28px;z-index:9998;width:56px;height:56px;border-radius:50%;',
        'background:linear-gradient(135deg,var(--brand,#0f6fff),var(--brand-deep,#0046b8));',
        'border:none;cursor:pointer;box-shadow:0 8px 24px rgba(15,111,255,0.38);',
        'display:flex;align-items:center;justify-content:center;transition:transform 0.2s ease,box-shadow 0.2s ease;}',
        '.chat-fab:hover{transform:translateY(-2px);box-shadow:0 14px 30px rgba(15,111,255,0.48);}',
        '.chat-fab.has-ping::after{content:"";position:absolute;top:3px;right:3px;width:12px;height:12px;',
        'border-radius:50%;background:#f4a340;border:2px solid var(--surface,#fff);}',
        '.chat-fab svg{width:26px;height:26px;fill:#fff;}',

        '.chat-panel{position:fixed;bottom:96px;right:20px;z-index:9999;width:min(560px,calc(100vw - 32px));',
        'background:var(--surface,#fff);border:1px solid var(--line,#dbe4f0);border-radius:22px;',
        'box-shadow:0 24px 60px rgba(8,24,44,0.22);display:flex;flex-direction:column;overflow:hidden;',
        'max-height:min(580px,calc(100vh - 140px));',
        'animation:chatSlideUp 0.3s cubic-bezier(0.22,0.61,0.36,1) both;}',
        '.chat-panel.hidden{display:none!important;}',

        '.chat-header{display:flex;align-items:center;gap:12px;padding:16px 18px;',
        'background:linear-gradient(135deg,var(--brand,#0f6fff),var(--brand-deep,#0046b8));',
        'color:#fff;flex-shrink:0;}',
        '.chat-header-avatar{width:38px;height:38px;border-radius:50%;background:rgba(255,255,255,0.2);',
        'display:flex;align-items:center;justify-content:center;flex-shrink:0;}',
        '.chat-header-avatar svg{width:20px;height:20px;fill:#fff;}',
        '.chat-header-info{flex:1;min-width:0;}',
        '.chat-header-name{font-size:0.9rem;font-weight:800;letter-spacing:0.01em;}',
        '.chat-header-status{font-size:0.72rem;opacity:0.85;display:flex;align-items:center;gap:5px;}',
        '.chat-status-dot{width:7px;height:7px;border-radius:50%;background:#7dffc5;flex-shrink:0;}',
        '.chat-close-btn{background:none;border:none;color:#fff;cursor:pointer;opacity:0.8;',
        'display:flex;align-items:center;justify-content:center;padding:4px;border-radius:8px;}',
        '.chat-close-btn:hover{opacity:1;background:rgba(255,255,255,0.15);}',
        '.chat-close-btn svg{width:18px;height:18px;}',

        '.chat-messages{flex:1;overflow-y:auto;overflow-x:auto;padding:16px;display:flex;flex-direction:column;gap:10px;',
        'scrollbar-width:thin;scrollbar-color:var(--line,#dbe4f0) transparent;}',
        '.chat-messages::-webkit-scrollbar{width:5px;}',
        '.chat-messages::-webkit-scrollbar-thumb{background:var(--line,#dbe4f0);border-radius:4px;}',

        '.chat-msg{display:flex;flex-direction:column;gap:3px;animation:chatMsgIn 0.22s ease both;width:100%;max-width:100%;}',
        '.chat-msg.user{align-self:flex-end;align-items:flex-end;}',
        '.chat-msg.bot{align-self:stretch;align-items:stretch;}',
        '.chat-bubble{padding:10px 14px;border-radius:16px;font-size:0.84rem;line-height:1.55;',
        'word-break:break-word;overflow-wrap:anywhere;max-width:100%;}',
        '.chat-msg.bot .chat-bubble{width:100%;}',
        '.chat-msg.user .chat-bubble{background:linear-gradient(135deg,var(--brand,#0f6fff),var(--brand-deep,#0046b8));',
        'color:#fff;border-bottom-right-radius:4px;}',
        '.chat-msg.user .chat-bubble{width:auto;max-width:min(86%,420px);}',
        '.chat-msg.bot .chat-bubble{background:var(--bg,#f4f7fb);color:var(--ink,#13233a);',
        'border:1px solid var(--line,#dbe4f0);border-bottom-left-radius:4px;}',
        '.chat-time{font-size:0.68rem;color:var(--muted,#5f6f85);padding:0 4px;}',

        '.chat-bubble table{width:100%;border-collapse:collapse;margin-top:8px;font-size:0.8rem;table-layout:fixed;}',
        '.chat-bubble th,.chat-bubble td{text-align:left;padding:5px 7px;border-bottom:1px solid var(--line,#dbe4f0);vertical-align:top;}',
        '.chat-bubble th,.chat-bubble td{word-break:break-word;overflow-wrap:anywhere;}',
        '.chat-bubble th{font-size:0.72rem;text-transform:uppercase;letter-spacing:0.04em;opacity:0.65;}',
        '.chat-bubble td:last-child,.chat-bubble th:last-child{text-align:right;}',
        '.chat-help-table th,.chat-help-table td{text-align:left!important;}',
        '.chat-options{display:flex;flex-wrap:wrap;gap:6px;margin-top:10px;}',
        '.chat-option{border:1px solid var(--line,#dbe4f0);background:var(--surface,#fff);',
        'color:var(--brand,#0f6fff);border-radius:999px;padding:5px 11px;font-size:0.76rem;',
        'font-weight:700;cursor:pointer;transition:background 0.15s ease,color 0.15s ease;}',
        '.chat-option:hover{background:var(--brand,#0f6fff);color:#fff;border-color:transparent;}',
        '.chat-badge{display:inline-flex;border-radius:999px;padding:1px 7px;font-size:0.72rem;',
        'font-weight:700;background:var(--chip-bg,#eef4ff);color:var(--chip-ink,#16407a);}',
        '.chat-badge.success{background:#e8fbf5;color:#076e52;}',
        '.chat-badge.failed{background:#ffecee;color:#a12626;}',
        '.chat-badge.pending{background:#fff4e0;color:#93590a;}',

        '.chat-chips{padding:6px 12px 10px;display:flex;gap:6px;flex-wrap:wrap;flex-shrink:0;',
        'border-top:1px solid var(--line,#dbe4f0);background:var(--bg,#f4f7fb);}',
        '.chat-chip{border:1px solid var(--line,#dbe4f0);background:var(--surface,#fff);',
        'color:var(--brand,#0f6fff);border-radius:999px;padding:5px 11px;font-size:0.76rem;',
        'font-weight:700;cursor:pointer;transition:background 0.15s ease,color 0.15s ease;white-space:nowrap;}',
        '.chat-chip:hover{background:var(--brand,#0f6fff);color:#fff;border-color:transparent;}',

        '.chat-input-row{display:flex;gap:8px;padding:10px 14px;border-top:1px solid var(--line,#dbe4f0);',
        'background:var(--surface,#fff);flex-shrink:0;}',
        '.chat-input{flex:1;border:1px solid var(--line,#dbe4f0);background:var(--bg,#f4f7fb);',
        'color:var(--ink,#13233a);border-radius:12px;padding:9px 12px;font:inherit;font-size:0.84rem;',
        'outline:none;transition:border-color 0.18s ease,box-shadow 0.18s ease;}',
        '.chat-input:focus{border-color:var(--brand,#0f6fff);box-shadow:0 0 0 3px rgba(15,111,255,0.12);}',
        '.chat-send-btn{width:38px;height:38px;border-radius:50%;background:linear-gradient(135deg,',
        'var(--brand,#0f6fff),var(--brand-deep,#0046b8));border:none;cursor:pointer;',
        'display:flex;align-items:center;justify-content:center;flex-shrink:0;',
        'box-shadow:0 4px 12px rgba(15,111,255,0.3);transition:transform 0.15s ease;}',
        '.chat-send-btn:hover{transform:scale(1.08);}',
        '.chat-send-btn svg{width:16px;height:16px;fill:#fff;}',

        '.typing-indicator{display:flex;gap:5px;align-items:center;padding:10px 14px;',
        'background:var(--bg,#f4f7fb);border:1px solid var(--line,#dbe4f0);border-radius:16px;',
        'border-bottom-left-radius:4px;width:fit-content;}',
        '.typing-dot{width:7px;height:7px;border-radius:50%;background:var(--muted,#5f6f85);',
        'animation:typingBounce 1.1s ease-in-out infinite;}',
        '.typing-dot:nth-child(2){animation-delay:0.18s;}',
        '.typing-dot:nth-child(3){animation-delay:0.36s;}',

        '@media(max-width:480px){.chat-panel{bottom:80px;right:10px;width:calc(100vw - 20px);}',
        '.chat-fab{bottom:18px;right:18px;}}'
    ].join('');
    document.head.appendChild(styleEl);

    /* ------------------------------------------------------------------
       DOM INJECTION
    ------------------------------------------------------------------ */
    var host = document.createElement('div');
    host.id = 'chat-host';
    host.innerHTML = [
        '<button class="chat-fab has-ping" id="chat-fab" aria-label="Open payment assistant" title="Payment Assistant">',
        '<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">',
        '<path d="M20 2H4C2.9 2 2 2.9 2 4v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>',
        '</svg>',
        '</button>',

        '<div class="chat-panel hidden" id="chat-panel" role="dialog" aria-label="Payment Assistant">',

        '<div class="chat-header">',
        '<div class="chat-header-avatar">',
        '<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">',
        '<path d="M19 3H5c-1.1 0-2 .9-2 2v14l4-4h12c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>',
        '</svg>',
        '</div>',
        '<div class="chat-header-info">',
        '<div class="chat-header-name">Payment Assistant</div>',
        '<div class="chat-header-status"><span class="chat-status-dot"></span>Connected to live data</div>',
        '</div>',
        '<button class="chat-close-btn" id="chat-close-btn" aria-label="Close chat">',
        '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">',
        '<path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
        '</svg>',
        '</button>',
        '</div>',

        '<div class="chat-messages" id="chat-messages"></div>',

        '<div class="chat-chips" id="chat-chips">',
        '<button class="chat-chip" data-query="System summary">Summary</button>',
        '<button class="chat-chip" data-query="Show failed payments">Failed</button>',
        '<button class="chat-chip" data-query="Error code breakdown">Errors</button>',
        '<button class="chat-chip" data-query="Currency volumes">Currencies</button>',
        '<button class="chat-chip" data-query="Recent payment history">Recent</button>',
        '<button class="chat-chip" data-query="Help">Help</button>',
        '</div>',

        '<div class="chat-input-row">',
        '<input class="chat-input" id="chat-input" type="text" placeholder="Ask me anything about payments..." autocomplete="off">',
        '<button class="chat-send-btn" id="chat-send-btn" aria-label="Send">',
        '<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">',
        '<path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>',
        '</svg>',
        '</button>',
        '</div>',

        '</div>'
    ].join('');
    document.body.appendChild(host);

    var fabEl      = document.getElementById('chat-fab');
    var panelEl    = document.getElementById('chat-panel');
    var closeBtn   = document.getElementById('chat-close-btn');
    var inputEl    = document.getElementById('chat-input');
    var sendBtn    = document.getElementById('chat-send-btn');
    var messagesEl = document.getElementById('chat-messages');
    var chipsEl    = document.getElementById('chat-chips');

    /* ------------------------------------------------------------------
       KNOWLEDGE BASE
    ------------------------------------------------------------------ */
    var KB = {
        'CREATED':            'A payment was submitted and is waiting to start processing.',
        'VALIDATED':          'The payment passed all input validations (account, currency, amount checks).',
        'SENT':               'The payment was dispatched to the destination and is awaiting settlement.',
        'COMPLETED':          'The payment was fully settled and funds have been transferred.',
        'FAILED':             'The payment could not complete. Check the error code for the exact reason.',
        'INSUFFICIENT_FUNDS': 'The source account did not have enough balance to cover the payment.',
        'INVALID_ACCOUNT':    'The source or destination account number was not found or is inactive.',
        'INVALID_CURRENCY':   'The payment used a currency that is not supported by the system.',
        'INVALID_AMOUNT':     'The payment amount was zero, negative, or exceeded allowed limits.',
        'NETWORK_ERROR':      'A connectivity issue occurred during external settlement — worth retrying.',
        'PROCESSING_ERROR':   'An internal processing error occurred. Often transient, check logs for details.',
        'VALIDATION_FAILED':  'General validation failure — the payment did not pass input checks before processing.',
        'lifecycle':          'Payments flow through: CREATED → VALIDATED → SENT → COMPLETED. At any stage a payment can transition to FAILED with an error code explaining the reason.',
        'error codes':        'Error codes identify why a payment failed. Common ones include INSUFFICIENT_FUNDS, INVALID_ACCOUNT, NETWORK_ERROR, PROCESSING_ERROR, and VALIDATION_FAILED.',
        'currencies':         'The system supports USD (US Dollar), EUR (Euro), GBP (British Pound), and INR (Indian Rupee).',
        'health score':       'The health score (0–100) measures execution quality. 80+ is healthy (green), 60–79 is cautionary (amber), below 60 needs attention (red).',
        'dashboard':          'The dashboard shows live KPI counts, scoped to a selected time range, with period-over-period deltas and a payment health score.',
        'analytics':          'The analytics page lets you filter by status, currency, error code, date range, and amount. It shows charts for status distribution, error breakdown, currency volume, trends, and FX rates.'
    };

    /* ------------------------------------------------------------------
       INTENT PATTERNS
    ------------------------------------------------------------------ */
    var INTENTS = [
        { name: 'help',           patterns: [/\bhelp\b/i, /what can you/i, /\bcommands\b/i, /what do you do/i, /capabilities/i] },
        { name: 'summary',        patterns: [/\bsummar(y|ise|ize)\b/i, /\boverview\b/i, /\bbriefing\b/i, /how are we doing/i, /\bhow.s it going\b/i, /system status/i] },
        { name: 'failed',         patterns: [/\bfail(ed|ures?|ing)?\b/i, /\brejected\b/i] },
        { name: 'pending',        patterns: [/\bpending\b/i, /\bin.?flight\b/i, /\bprocessing\b/i, /not.*completed/i] },
        { name: 'succeeded',      patterns: [/\bsuccess(ful|ed|es|ful)?\b/i, /\bcomplete(d)?\b/i] },
        { name: 'error_codes',    patterns: [/error.?code/i, /top.*error/i, /\binsufficient.?funds\b/i, /\binvalid.?account\b/i, /\bnetwork.?error\b/i, /\bprocessing.?error\b/i, /\bvalidation.?fail/i, /failure reason/i, /why.*fail/i] },
        { name: 'payment_lookup', patterns: [/payment\s*#?\s*(\d+)/i, /\bid\s*#?\s*(\d+)/i, /find\s+(payment\s*)?#?\s*(\d+)/i, /lookup\s+#?\s*(\d+)/i] },
        { name: 'currency',       patterns: [/\b(usd|eur|gbp|inr)\b/i, /currencies/i, /currency.?volume/i, /by currency/i, /money.?transfer/i] },
        { name: 'trend',          patterns: [/\btrend\b/i, /over time/i, /\bmonthly\b/i, /last (month|week)/i, /\bhistory\b/i, /volume.*over/i] },
        { name: 'health',         patterns: [/\bhealth\b/i, /\bhealth.?score\b/i, /\bhow.s the system\b/i, /risk/i] },
        { name: 'recent',         patterns: [/\brecent\b/i, /\blatest\b/i, /last \d+ payments?/i, /new(est)? payments?/i] },
        { name: 'explain',        patterns: [/\bexplain\b/i, /\bwhat (is|does|are)\b/i, /\bmean\b/i, /\blifecycle\b/i, /tell me about/i, /describe\b/i] }
    ];

    function classifyIntent(text) {
        for (var i = 0; i < INTENTS.length; i++) {
            for (var j = 0; j < INTENTS[i].patterns.length; j++) {
                if (INTENTS[i].patterns[j].test(text)) {
                    return INTENTS[i].name;
                }
            }
        }
        return 'unknown';
    }

    function extractId(text) {
        var match = text.match(/(?:payment|id|find|lookup)\s*#?\s*(\d+)/i) || text.match(/#(\d+)/);
        return match ? match[1] : null;
    }

    function extractExplainTopic(text) {
        var lower = text.toLowerCase();
        var keys = Object.keys(KB);
        for (var i = 0; i < keys.length; i++) {
            if (lower.indexOf(keys[i].toLowerCase()) !== -1) {
                return keys[i];
            }
        }
        return null;
    }

    /* ------------------------------------------------------------------
       API HELPERS
    ------------------------------------------------------------------ */
    async function api(url) {
        var response = await fetch(url);
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return response.json();
    }

    function formatNum(n) {
        return Number(n || 0).toLocaleString(undefined, { maximumFractionDigits: 2 });
    }

    function formatPct(n) {
        return Number(n || 0).toFixed(1) + '%';
    }

    function pct(num, total) {
        return total ? ((num / total) * 100).toFixed(1) + '%' : '0%';
    }

    function badgeHtml(status) {
        var s = String(status || '').toLowerCase();
        var cls = (s.indexOf('complet') !== -1 || s.indexOf('success') !== -1) ? 'success'
                : (s.indexOf('fail') !== -1 || s.indexOf('reject') !== -1) ? 'failed'
                : 'pending';
        return '<span class="chat-badge ' + cls + '">' + escHtml(status) + '</span>';
    }

    function escHtml(s) {
        return String(s || '-').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function nowTime() {
        return new Date().toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
    }

    /* ------------------------------------------------------------------
       RESPONSE BUILDERS
    ------------------------------------------------------------------ */
    async function handleSummary() {
        var [overview, currency] = await Promise.all([
            api(ANALYTICS + '/overview'),
            api(ANALYTICS + '/currency-volume')
        ]);

        var total = overview.totalPayments || 0;
        var topCurr = (currency || []).slice().sort(function (a, b) { return b.totalAmount - a.totalAmount; })[0];

        var html = [
            '<strong>Payment System Overview</strong>',
            '<table><thead><tr><th>Metric</th><th>Value</th></tr></thead><tbody>',
            '<tr><td>Total Payments</td><td>' + formatNum(total) + '</td></tr>',
            '<tr><td>Success Rate</td><td><span class="chat-badge success">' + formatPct(overview.successRate) + '</span></td></tr>',
            '<tr><td>Failure Rate</td><td><span class="chat-badge failed">' + formatPct(overview.failureRate) + '</span></td></tr>',
            '<tr><td>In-Flight</td><td><span class="chat-badge pending">' + formatNum(overview.inFlightCount) + '</span></td></tr>',
            '<tr><td>Completed</td><td>' + formatNum(overview.completedCount) + '</td></tr>',
            '<tr><td>Failed</td><td>' + formatNum(overview.failedCount) + '</td></tr>',
            '</tbody></table>'
        ];

        if (topCurr) {
            html.push('<br>Top currency by volume: <strong>' + escHtml(topCurr.currency) + '</strong> with ' + formatNum(topCurr.totalAmount) + ' across ' + formatNum(topCurr.count) + ' payments.');
        }

        return html.join('');
    }

    async function handleFailed() {
        var [overview, errors] = await Promise.all([
            api(ANALYTICS + '/overview?status=FAILED'),
            api(ANALYTICS + '/error-codes')
        ]);

        var total = overview.totalPayments || 0;
        var html = [
            '<strong>Failed Payments</strong><br>',
            'There are <strong>' + formatNum(total) + '</strong> failed payment(s) on record.'
        ];

        if (errors && errors.length) {
            html.push('<br><br><strong>Top failure reasons:</strong>');
            html.push('<table><thead><tr><th>Error Code</th><th>Count</th><th>Share</th></tr></thead><tbody>');
            var totalFailed = errors.reduce(function (s, e) { return s + (e.count || 0); }, 0);
            errors.slice(0, 5).forEach(function (e) {
                html.push('<tr><td>' + escHtml(e.errorCode) + '</td><td>' + formatNum(e.count) + '</td><td>' + pct(e.count, totalFailed) + '</td></tr>');
            });
            html.push('</tbody></table>');
            html.push('<br>Tip: Click <em>Error Codes</em> in Analytics for the full breakdown.');
        } else {
            html.push('<br>No error codes recorded yet.');
        }

        return html.join('');
    }

    async function handlePending() {
        var overview = await api(ANALYTICS + '/overview');
        var inFlight = overview.inFlightCount || 0;
        var total = overview.totalPayments || 0;

        return [
            '<strong>In-Flight Payments</strong><br>',
            'Currently <strong>' + formatNum(inFlight) + '</strong> payment(s) are in-flight (' + pct(inFlight, total) + ' of all payments).',
            '<br><br>These are payments with status CREATED, VALIDATED, or SENT that have not yet reached a terminal state.'
        ].join('');
    }

    async function handleSucceeded() {
        var overview = await api(ANALYTICS + '/overview');
        return [
            '<strong>Successful Payments</strong><br>',
            '<strong>' + formatNum(overview.completedCount) + '</strong> payments completed successfully.',
            '<br>That\'s a success rate of <span class="chat-badge success">' + formatPct(overview.successRate) + '</span> of all payments.'
        ].join('');
    }

    async function handleErrorCodes() {
        var errors = await api(ANALYTICS + '/error-codes');
        if (!errors || !errors.length) {
            return 'No error codes recorded yet. All payments may have succeeded or none have failed with coded errors.';
        }

        var total = errors.reduce(function (s, e) { return s + (e.count || 0); }, 0);
        var html = [
            '<strong>Error Code Breakdown</strong> (' + formatNum(total) + ' total failed)<br>',
            '<table><thead><tr><th>Error Code</th><th>Count</th><th>%</th></tr></thead><tbody>'
        ];

        errors.forEach(function (e) {
            html.push('<tr><td>' + escHtml(e.errorCode) + '</td><td>' + formatNum(e.count) + '</td><td>' + pct(e.count, total) + '</td></tr>');
        });

        html.push('</tbody></table>');
        html.push('<br>Ask me to <em>explain</em> any error code for details.');
        return html.join('');
    }

    async function handlePaymentLookup(text) {
        var id = extractId(text);
        if (!id) {
            return 'Please include a payment ID — for example: <em>find payment 42</em>';
        }

        var payment = null;

        try {
            payment = await api(PAYMENTS + '/' + encodeURIComponent(id));
        } catch (e) {
            try {
                var all = await api(PAYMENTS);
                payment = Array.isArray(all)
                    ? all.find(function (p) { return String(p.id) === String(id); })
                    : null;
            } catch (e2) {
                return 'Could not reach the payments API. Make sure the backend is running.';
            }
        }

        if (!payment) {
            return 'No payment found with ID <strong>' + escHtml(id) + '</strong>.';
        }

        var history = [];
        try {
            history = await api(HISTORY + '/payment/' + encodeURIComponent(id));
        } catch (e) {
            try {
                var allHist = await api(HISTORY);
                history = Array.isArray(allHist)
                    ? allHist.filter(function (h) { return String(h.paymentId) === String(id); })
                    : [];
            } catch (e2) { /* silent */ }
        }

        history.sort(function (a, b) { return new Date(a.createdAt) - new Date(b.createdAt); });

        var html = [
            '<strong>Payment #' + escHtml(id) + '</strong><br>',
            '<table><thead><tr><th>Field</th><th>Value</th></tr></thead><tbody>',
            '<tr><td>Status</td><td>' + badgeHtml(payment.status) + '</td></tr>',
            '<tr><td>Amount</td><td>' + formatNum(payment.amount) + ' ' + escHtml(payment.currency) + '</td></tr>',
            '<tr><td>From</td><td>' + escHtml(payment.sourceAccount) + '</td></tr>',
            '<tr><td>To</td><td>' + escHtml(payment.destinationAccount) + '</td></tr>',
            '<tr><td>Created</td><td>' + (payment.createdAt ? new Date(payment.createdAt).toLocaleString() : '-') + '</td></tr>',
            payment.errorCode ? '<tr><td>Error</td><td><span style="color:var(--danger,#c23a3a);font-weight:700">' + escHtml(payment.errorCode) + '</span></td></tr>' : '',
            '</tbody></table>'
        ];

        if (history.length) {
            html.push('<br><strong>Lifecycle:</strong>');
            history.forEach(function (h, idx) {
                var arrow = idx < history.length - 1 ? ' →' : '';
                html.push('<br>' + badgeHtml(h.status) + (h.note ? ' <small>(' + escHtml(h.note) + ')</small>' : '') + arrow);
            });
        }

        return html.join('');
    }

    async function handleCurrency() {
        var volumes = await api(ANALYTICS + '/currency-volume');
        if (!volumes || !volumes.length) {
            return 'No currency volume data available yet.';
        }

        volumes.sort(function (a, b) { return b.totalAmount - a.totalAmount; });
        var totalVol = volumes.reduce(function (s, c) { return s + (c.totalAmount || 0); }, 0);

        var html = [
            '<strong>Currency Volume Breakdown</strong><br>',
            '<table><thead><tr><th>Currency</th><th>Amount</th><th>Payments</th><th>Share</th></tr></thead><tbody>'
        ];

        volumes.forEach(function (v) {
            html.push('<tr><td><strong>' + escHtml(v.currency) + '</strong></td><td>' + formatNum(v.totalAmount) + '</td><td>' + formatNum(v.count) + '</td><td>' + pct(v.totalAmount, totalVol) + '</td></tr>');
        });

        html.push('</tbody></table>');
        return html.join('');
    }

    async function handleTrend() {
        var trend = await api(ANALYTICS + '/trend');
        if (!trend || !trend.length) {
            return 'No trend data available yet — payments need to span more than one period.';
        }

        var recent = trend.slice(-6);
        var html = [
            '<strong>Payment Volume Trend (recent periods)</strong><br>',
            '<table><thead><tr><th>Period</th><th>Payments</th><th>Amount</th></tr></thead><tbody>'
        ];

        recent.forEach(function (row) {
            html.push('<tr><td>' + escHtml(row.periodLabel) + '</td><td>' + formatNum(row.count) + '</td><td>' + formatNum(row.totalAmount) + '</td></tr>');
        });

        html.push('</tbody></table>');
        html.push('<br>Head to the <em>Analytics</em> page for an interactive trend chart.');
        return html.join('');
    }

    async function handleHealth() {
        var overview = await api(ANALYTICS + '/overview');
        var total = overview.totalPayments || 0;
        if (!total) {
            return 'No payment data yet to assess system health.';
        }

        var successRate = overview.successRate || 0;
        var failureRate = overview.failureRate || 0;
        var inFlight = overview.inFlightCount || 0;
        var pendingPct = total ? (inFlight / total) * 100 : 0;

        var score = Math.round(Math.max(0, Math.min(100, 100 - (failureRate * 1.4) - (pendingPct * 0.6) + (successRate * 0.2))));

        var level, colour, advice;
        if (score >= 80) {
            level = 'Excellent';
            colour = '#0a8e67';
            advice = 'Payment execution is healthy. No immediate action required.';
        } else if (score >= 60) {
            level = 'Caution';
            colour = '#b46b00';
            advice = 'Failure rates are moderate. Review the top error codes on the Analytics page.';
        } else {
            level = 'At Risk';
            colour = '#c23a3a';
            advice = 'High failure rate detected. Investigate error codes and check account configurations.';
        }

        return [
            '<strong>Payment Health Score</strong><br>',
            '<span style="font-size:1.6rem;font-weight:800;color:' + colour + '">' + score + ' / 100</span>',
            ' &nbsp;<span style="color:' + colour + ';font-weight:700">' + level + '</span>',
            '<br><br>',
            '<table><thead><tr><th>Signal</th><th>Value</th></tr></thead><tbody>',
            '<tr><td>Success Rate</td><td><span class="chat-badge success">' + formatPct(successRate) + '</span></td></tr>',
            '<tr><td>Failure Rate</td><td><span class="chat-badge failed">' + formatPct(failureRate) + '</span></td></tr>',
            '<tr><td>In-Flight</td><td><span class="chat-badge pending">' + formatNum(inFlight) + '</span></td></tr>',
            '</tbody></table>',
            '<br>' + advice
        ].join('');
    }

    async function handleRecent() {
        var rows = await api(HISTORY + '/latest?limit=10');
        if (!Array.isArray(rows) || !rows.length) {
            return 'No recent payment history found.';
        }

        var html = [
            '<strong>Latest Payment Activity</strong><br>',
            '<table><thead><tr><th>Payment ID</th><th>Status</th><th>When</th></tr></thead><tbody>'
        ];

        rows.slice(0, 8).forEach(function (h) {
            html.push('<tr><td>' + escHtml(h.paymentId) + '</td><td>' + badgeHtml(h.status) + '</td><td>' + (h.createdAt ? new Date(h.createdAt).toLocaleString() : '-') + '</td></tr>');
        });

        html.push('</tbody></table>');
        return html.join('');
    }

    function handleExplain(text) {
        var topic = extractExplainTopic(text);
        if (!topic) {
            var keys = Object.keys(KB);
            return [
                'I can explain the following terms:<br>',
                keys.map(function (k) { return '<em>' + escHtml(k) + '</em>'; }).join(', '),
                '<br><br>Try: <em>explain INSUFFICIENT_FUNDS</em> or <em>what is the lifecycle</em>'
            ].join('');
        }

        return '<strong>' + escHtml(topic.toUpperCase()) + '</strong><br>' + escHtml(KB[topic]);
    }

    function handleHelp() {
        return [
            '<strong>What I can help you with:</strong><br>',
            '<table class="chat-help-table"><thead><tr><th>Try this</th></tr></thead><tbody>',
            '<tr><td><button type="button" class="chat-option" data-query="System overview">System overview</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Show me failed payments">Show me failed payments</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="How many are pending?">How many are pending?</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="How many succeeded?">How many succeeded?</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Error code breakdown">Error code breakdown</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Find payment #42">Find payment #42</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Currency volumes">Currency volumes</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Show me the volume trend">Show me the volume trend</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="What\'s the health score?">What\'s the health score?</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Recent payment history">Recent payment history</button></td></tr>',
            '<tr><td><button type="button" class="chat-option" data-query="Explain INSUFFICIENT_FUNDS">Explain INSUFFICIENT_FUNDS</button></td></tr>',
            '</tbody></table>',
            '<br>You can also tap a quick option:<div class="chat-options">',
            '<button type="button" class="chat-option" data-query="System summary">System summary</button>',
            '<button type="button" class="chat-option" data-query="Show failed payments">Failed payments</button>',
            '<button type="button" class="chat-option" data-query="Error code breakdown">Error codes</button>',
            '<button type="button" class="chat-option" data-query="Recent payment history">Recent history</button>',
            '<button type="button" class="chat-option" data-query="Health score">Health score</button>',
            '</div>'
        ].join('');
    }

    function handleUnknown() {
        return [
            'I didn\'t quite catch that. Here are some things I can help with:<br>',
            '<em>System summary</em> · <em>Show failed payments</em> · <em>Error code breakdown</em>',
            ' · <em>Find payment #42</em> · <em>Currency volumes</em> · <em>Health score</em>',
            '<br><br>Type <em>help</em> to see all available commands.'
        ].join('');
    }

    /* ------------------------------------------------------------------
       DISPATCHER
    ------------------------------------------------------------------ */
    async function dispatch(text) {
        var intent = classifyIntent(text);

        if (intent === 'help')           return handleHelp();
        if (intent === 'summary')        return handleSummary();
        if (intent === 'failed')         return handleFailed();
        if (intent === 'pending')        return handlePending();
        if (intent === 'succeeded')      return handleSucceeded();
        if (intent === 'error_codes')    return handleErrorCodes();
        if (intent === 'payment_lookup') return handlePaymentLookup(text);
        if (intent === 'currency')       return handleCurrency();
        if (intent === 'trend')          return handleTrend();
        if (intent === 'health')         return handleHealth();
        if (intent === 'recent')         return handleRecent();
        if (intent === 'explain')        return Promise.resolve(handleExplain(text));
        return Promise.resolve(handleUnknown());
    }

    /* ------------------------------------------------------------------
       UI HELPERS
    ------------------------------------------------------------------ */
    function scrollBottom() {
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    function addMessage(role, htmlContent) {
        var msg = document.createElement('div');
        msg.className = 'chat-msg ' + role;

        var bubble = document.createElement('div');
        bubble.className = 'chat-bubble';
        bubble.innerHTML = htmlContent;

        var time = document.createElement('div');
        time.className = 'chat-time';
        time.textContent = nowTime();

        msg.appendChild(bubble);
        msg.appendChild(time);
        messagesEl.appendChild(msg);
        scrollBottom();
        return msg;
    }

    function addTyping() {
        var msg = document.createElement('div');
        msg.className = 'chat-msg bot';
        msg.id = 'chat-typing';
        var bubble = document.createElement('div');
        bubble.className = 'typing-indicator';
        bubble.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';
        msg.appendChild(bubble);
        messagesEl.appendChild(msg);
        scrollBottom();
        return msg;
    }

    function removeTyping() {
        var t = document.getElementById('chat-typing');
        if (t) t.remove();
    }

    async function sendMessage(text) {
        text = (text || '').trim();
        if (!text) return;

        addMessage('user', escHtml(text));
        inputEl.value = '';

        var typingEl = addTyping();

        var minDelay = new Promise(function (resolve) { setTimeout(resolve, 600); });
        var responsePromise = dispatch(text).catch(function (err) {
            return 'Sorry, I ran into an error reaching the backend: <em>' + escHtml(String(err.message || err)) + '</em>. Make sure the payment server is running on port 8080.';
        });

        var result = await Promise.all([responsePromise, minDelay]);
        removeTyping();
        addMessage('bot', result[0]);
    }

    /* ------------------------------------------------------------------
       EVENT WIRING
    ------------------------------------------------------------------ */
    function openPanel() {
        panelEl.classList.remove('hidden');
        fabEl.classList.remove('has-ping');
        inputEl.focus();

        if (messagesEl.children.length === 0) {
            addMessage('bot', [
                'Hi! I\'m your <strong>Payment Assistant</strong>. I can help you understand your payment data using natural language.<br><br>',
                'Try asking: <em>"System summary"</em>, <em>"Show failed payments"</em>, <em>"Health score"</em>, or <em>"Find payment #1"</em>.<br><br>',
                'Or tap an option to continue:',
                '<div class="chat-options">',
                '<button type="button" class="chat-option" data-query="System summary">System summary</button>',
                '<button type="button" class="chat-option" data-query="Show failed payments">Failed payments</button>',
                '<button type="button" class="chat-option" data-query="Error code breakdown">Error code breakdown</button>',
                '<button type="button" class="chat-option" data-query="Recent payment history">Recent payment history</button>',
                '<button type="button" class="chat-option" data-query="Help">Help</button>',
                '</div>'
            ].join(''));
        }
    }

    function closePanel() {
        panelEl.classList.add('hidden');
    }

    fabEl.addEventListener('click', function () {
        panelEl.classList.contains('hidden') ? openPanel() : closePanel();
    });

    closeBtn.addEventListener('click', closePanel);

    sendBtn.addEventListener('click', function () {
        sendMessage(inputEl.value);
    });

    inputEl.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage(inputEl.value);
        }
    });

    chipsEl.addEventListener('click', function (e) {
        var chip = e.target.closest('.chat-chip');
        if (!chip) return;
        if (panelEl.classList.contains('hidden')) openPanel();
        sendMessage(chip.getAttribute('data-query'));
    });

    messagesEl.addEventListener('click', function (e) {
        var option = e.target.closest('[data-query]');
        if (!option) return;
        sendMessage(option.getAttribute('data-query'));
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && !panelEl.classList.contains('hidden')) closePanel();
    });

})();
