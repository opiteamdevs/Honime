/**
 * StarsninEngine - Motor de Renderização Web Independente
 * Restrição Técnica: PROIBIDO Chromium / WebViews Nativas / Electron CEF
 * Construído do zero em JavaScript Puro
 */

class StarsninEngine {
  constructor(viewportElement) {
    this.viewport = viewportElement;
    this.currentUrl = "https://starsnin.vercel.app";
    this.currentDom = null;
    this.rawHtml = "";
    this.telemetry = {
      tokens: 0,
      nodes: 0,
      renderTimeMs: 0
    };
    this.onNavigateCallback = null;
  }

  setNavigateCallback(cb) {
    this.onNavigateCallback = cb;
  }

  /**
   * Tokenizer & Lexer: Converte string HTML em lista de tokens
   */
  tokenize(html) {
    const tokens = [];
    let i = 0;
    const len = html.length;

    while (i < len) {
      if (html[i] === '<') {
        // Comment check
        if (html.startsWith('<!--', i)) {
          const end = html.indexOf('-->', i + 4);
          i = (end === -1) ? len : end + 3;
          continue;
        }

        // Doctype check
        if (html.substr(i, 9).toLowerCase() === '<!doctype') {
          const end = html.indexOf('>', i);
          i = (end === -1) ? len : end + 1;
          continue;
        }

        const endTag = html.indexOf('>', i);
        if (endTag === -1) break;

        const tagContent = html.substring(i + 1, endTag).trim();
        i = endTag + 1;

        if (tagContent.startsWith('/')) {
          tokens.push({ type: 'TAG_CLOSE', tag: tagContent.substring(1).trim().toLowerCase() });
        } else {
          const isSelfClose = tagContent.endsWith('/');
          const innerContent = isSelfClose ? tagContent.slice(0, -1).trim() : tagContent;
          const firstSpace = innerContent.indexOf(' ');
          const tagName = (firstSpace === -1 ? innerContent : innerContent.substring(0, firstSpace)).toLowerCase();
          const attrString = firstSpace === -1 ? '' : innerContent.substring(firstSpace).trim();
          const attributes = this.parseAttributes(attrString);

          tokens.push({
            type: isSelfClose ? 'TAG_SELF_CLOSE' : 'TAG_OPEN',
            tag: tagName,
            attributes: attributes
          });
        }
      } else {
        const nextTag = html.indexOf('<', i);
        const textSegment = nextTag === -1 ? html.substring(i) : html.substring(i, nextTag);
        const decoded = this.decodeHtml(textSegment.trim());
        if (decoded.length > 0) {
          tokens.push({ type: 'TEXT', text: decoded });
        }
        i = (nextTag === -1) ? len : nextTag;
      }
    }

    this.telemetry.tokens = tokens.length;
    return tokens;
  }

  parseAttributes(attrStr) {
    const attrs = {};
    const regex = /([a-zA-Z0-9_\-]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+)))?/g;
    let match;
    while ((match = regex.exec(attrStr)) !== null) {
      const key = match[1].toLowerCase();
      const val = match[2] !== undefined ? match[2] : (match[3] !== undefined ? match[3] : (match[4] || ''));
      attrs[key] = val;
    }
    return attrs;
  }

  decodeHtml(text) {
    return text
      .replace(/&amp;/g, '&')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&quot;/g, '"')
      .replace(/&#39;/g, "'")
      .replace(/&nbsp;/g, ' ');
  }

  /**
   * Construtor da Árvore DOM
   */
  buildDomTree(tokens, baseUrl) {
    const root = { tag: 'document', children: [], attributes: {}, styles: {} };
    const stack = [root];
    const selfClosingTags = new Set([
      'area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input',
      'link', 'meta', 'param', 'source', 'track', 'wbr'
    ]);

    let nodeCount = 0;

    for (const token of tokens) {
      if (token.type === 'TAG_OPEN') {
        const node = {
          tag: token.tag,
          attributes: token.attributes,
          styles: this.parseInlineStyles(token.attributes.style),
          children: []
        };
        nodeCount++;

        if (selfClosingTags.has(token.tag)) {
          stack[stack.length - 1].children.push(node);
        } else {
          stack[stack.length - 1].children.push(node);
          stack.push(node);
        }
      } else if (token.type === 'TAG_SELF_CLOSE') {
        nodeCount++;
        stack[stack.length - 1].children.push({
          tag: token.tag,
          attributes: token.attributes,
          styles: this.parseInlineStyles(token.attributes.style),
          children: []
        });
      } else if (token.type === 'TAG_CLOSE') {
        if (stack.length > 1) {
          // Find matching tag from top
          for (let k = stack.length - 1; k > 0; k--) {
            if (stack[k].tag === token.tag) {
              stack.splice(k);
              break;
            }
          }
        }
      } else if (token.type === 'TEXT') {
        nodeCount++;
        stack[stack.length - 1].children.push({
          type: 'TEXT',
          text: token.text
        });
      }
    }

    this.telemetry.nodes = nodeCount;
    return root;
  }

  parseInlineStyles(styleStr) {
    if (!styleStr) return {};
    const styles = {};
    styleStr.split(';').forEach(pair => {
      const idx = pair.indexOf(':');
      if (idx !== -1) {
        const k = pair.substring(0, idx).trim().toLowerCase();
        const v = pair.substring(idx + 1).trim();
        if (k && v) styles[k] = v;
      }
    });
    return styles;
  }

  /**
   * Pipeline de Renderização Independente no Canvas/Container
   */
  render(html, url) {
    const t0 = performance.now();
    this.rawHtml = html;
    this.currentUrl = url;

    const tokens = this.tokenize(html);
    const domTree = this.buildDomTree(tokens, url);
    this.currentDom = domTree;

    // Limpa a viewport
    this.viewport.innerHTML = '';

    const container = document.createElement('div');
    container.className = 'rendered-document';

    // Se a URL for a oficial do Starsnin, adiciona o Portal Interativo Oficial Starsnin
    if (url.includes('starsnin.vercel.app')) {
      container.appendChild(this.buildStarsninOfficialHome());
    }

    // Renderiza a árvore de nós recursivamente
    for (const child of domTree.children) {
      const renderedNode = this.renderNode(child, url);
      if (renderedNode) container.appendChild(renderedNode);
    }

    this.viewport.appendChild(container);
    this.telemetry.renderTimeMs = Math.round(performance.now() - t0);
    this.updateTelemetryUI();
  }

  renderNode(node, baseUrl) {
    if (node.type === 'TEXT') {
      return document.createTextNode(node.text + ' ');
    }

    const tag = node.tag ? node.tag.toLowerCase() : 'div';
    if (['head', 'title', 'meta', 'link', 'style', 'script'].includes(tag)) {
      return null;
    }

    const el = document.createElement(tag);

    // Copia atributos
    if (node.attributes) {
      for (const [key, val] of Object.entries(node.attributes)) {
        if (key === 'href' && tag === 'a') {
          el.href = '#';
          el.onclick = (e) => {
            e.preventDefault();
            if (this.onNavigateCallback) this.onNavigateCallback(val);
          };
        } else if (key === 'src' && tag === 'img') {
          el.src = val;
          el.alt = node.attributes.alt || 'Imagem';
        } else {
          el.setAttribute(key, val);
        }
      }
    }

    // Aplica estilos inline
    if (node.styles) {
      for (const [sKey, sVal] of Object.entries(node.styles)) {
        el.style[sKey] = sVal;
      }
    }

    // Manipulação de formulário / input
    if (tag === 'input') {
      el.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && el.value.trim()) {
          if (this.onNavigateCallback) this.onNavigateCallback(el.value.trim());
        }
      });
    }

    // Renderiza filhos
    if (node.children) {
      for (const child of node.children) {
        const childEl = this.renderNode(child, baseUrl);
        if (childEl) el.appendChild(childEl);
      }
    }

    return el;
  }

  buildStarsninOfficialHome() {
    const homeSection = document.createElement('div');
    homeSection.style.textAlign = 'center';
    homeSection.style.padding = '30px 10px';
    homeSection.style.borderBottom = '1px solid #334155';
    homeSection.style.marginBottom = '24px';

    homeSection.innerHTML = `
      <div style="margin-bottom: 20px;">
        <img src="https://starsnin.vercel.app/starsninlogo.png" 
             alt="Logo Oficial Starsnin" 
             style="width: 90px; height: 90px; border-radius: 50%; border: 3px solid #38bdf8; box-shadow: 0 0 20px rgba(56,189,248,0.4);" />
        <h1 style="color: #38bdf8; margin-top: 12px; font-size: 32px; font-weight: 800;">Starsnin</h1>
        <p style="color: #94a3b8; font-size: 14px; margin-top: 4px;">Motor de Busca Oficial & Ecossistema Independente</p>
      </div>
      <div style="max-width: 540px; margin: 0 auto; display: flex; gap: 8px;">
        <input type="text" id="starsnin-home-search" 
               placeholder="Pesquisar no Starsnin ou digitar URL..." 
               style="flex: 1; padding: 12px 18px; border-radius: 12px; background: #111827; border: 1.5px solid #38bdf8; color: #f8fafc; font-size: 14px; outline: none;" />
        <button id="starsnin-home-btn" 
                style="padding: 12px 24px; border-radius: 12px; background: #38bdf8; color: #003544; font-weight: 700; border: none; cursor: pointer;">
          Buscar
        </button>
      </div>
      <div style="display: flex; justify-content: center; gap: 12px; margin-top: 24px; flex-wrap: wrap;">
        <div style="background: #111827; border: 1px solid #1e293b; padding: 12px 20px; border-radius: 10px; font-size: 13px; color: #e2e8f0;">
          ⚡ Zero Chromium
        </div>
        <div style="background: #111827; border: 1px solid #1e293b; padding: 12px 20px; border-radius: 10px; font-size: 13px; color: #e2e8f0;">
          🛡️ Sem WebViews Nativas
        </div>
        <div style="background: #111827; border: 1px solid #1e293b; padding: 12px 20px; border-radius: 10px; font-size: 13px; color: #e2e8f0;">
          📐 Abas Verticais Laterais
        </div>
      </div>
    `;

    setTimeout(() => {
      const input = document.getElementById('starsnin-home-search');
      const btn = document.getElementById('starsnin-home-btn');
      if (btn && input) {
        const doSearch = () => {
          const val = input.value.trim();
          if (val && this.onNavigateCallback) this.onNavigateCallback(val);
        };
        btn.onclick = doSearch;
        input.onkeydown = (e) => { if (e.key === 'Enter') doSearch(); };
      }
    }, 100);

    return homeSection;
  }

  updateTelemetryUI() {
    const el = document.getElementById('telemetry-info');
    if (el) {
      el.textContent = `Engine: OK (${this.telemetry.renderTimeMs}ms) • ${this.telemetry.nodes} nós DOM • ${this.telemetry.tokens} tokens`;
    }
  }
}
