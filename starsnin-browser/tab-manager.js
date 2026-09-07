/**
 * TabManager - Gerenciador de Abas Verticais Laterais
 */

class TabManager {
  constructor(sidebarElement, onTabChange) {
    this.container = sidebarElement.querySelector('.tabs-list');
    this.onTabChange = onTabChange;
    this.tabs = [];
    this.activeTabId = null;
  }

  createTab(title = 'Starsnin', url = 'https://starsnin.vercel.app') {
    const tabId = 'tab_' + Math.random().toString(36).substr(2, 9);
    const tab = {
      id: tabId,
      title: title,
      url: url,
      history: [url],
      historyIndex: 0
    };

    this.tabs.push(tab);
    this.renderTabs();
    this.selectTab(tabId);
    return tab;
  }

  selectTab(tabId) {
    const tab = this.tabs.find(t => t.id === tabId);
    if (!tab) return;
    this.activeTabId = tabId;
    this.renderTabs();
    if (this.onTabChange) {
      this.onTabChange(tab);
    }
  }

  closeTab(tabId, e) {
    if (e) e.stopPropagation();
    if (this.tabs.length <= 1) return;

    const idx = this.tabs.findIndex(t => t.id === tabId);
    if (idx === -1) return;

    this.tabs.splice(idx, 1);

    if (this.activeTabId === tabId) {
      const nextTab = this.tabs[Math.max(0, idx - 1)];
      this.selectTab(nextTab.id);
    } else {
      this.renderTabs();
    }
  }

  updateActiveTabUrl(url, title) {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    if (tab) {
      if (tab.url !== url) {
        tab.history = tab.history.slice(0, tab.historyIndex + 1);
        tab.history.push(url);
        tab.historyIndex = tab.history.length - 1;
      }
      tab.url = url;
      if (title) tab.title = title;
      this.renderTabs();
    }
  }

  getActiveTab() {
    return this.tabs.find(t => t.id === this.activeTabId) || null;
  }

  renderTabs() {
    this.container.innerHTML = '';
    this.tabs.forEach(tab => {
      const tabEl = document.createElement('div');
      tabEl.className = `tab-item ${tab.id === this.activeTabId ? 'active' : ''}`;
      tabEl.onclick = () => this.selectTab(tab.id);

      tabEl.innerHTML = `
        <div class="tab-info">
          <div class="tab-dot"></div>
          <span class="tab-title">${this.escapeHtml(tab.title)}</span>
        </div>
        ${this.tabs.length > 1 ? `<button class="tab-close" title="Fechar aba">×</button>` : ''}
      `;

      const closeBtn = tabEl.querySelector('.tab-close');
      if (closeBtn) {
        closeBtn.onclick = (e) => this.closeTab(tab.id, e);
      }

      this.container.appendChild(tabEl);
    });
  }

  escapeHtml(str) {
    return (str || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }
}
