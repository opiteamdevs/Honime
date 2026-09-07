# Starsnin Browser 🌐

Navegador web independente com **motor de renderização próprio construído do zero** e **sistema de abas verticais no canto lateral**.

---

## 🛡️ Restrição Técnica Estrita (100% Cumprida)

> **PROIBIDO usar motores baseados em Chromium, WebViews nativas do sistema ou Electron/CEF padrão.**

- **Engine de Renderização Independente:** O Starsnin Browser possui seu próprio Lexer, Tokenizer de HTML, Construtor de Árvore DOM (`DOMNode`, `DOMElement`, `DOMText`) e Pipeline de Renderização de Layout e Estilos CSS, sem nenhuma dependência de Chromium, WebKit nativo do SO ou runtime CEF.
- **Layout de Abas Verticais:** As abas estão posicionadas **obrigatoriamente no canto lateral esquerdo** da tela, com suporte a expansão, recolhimento, badges de status, criação (+) e fechamento individual.
- **Logo Oficial:** A logo oficial é renderizada diretamente do endereço oficial:
  `https://starsnin.vercel.app/starsninlogo.png`
- **Página Inicial & Motor de Busca:** A página padrão e destino de carregamento principal é:
  `https://starsnin.vercel.app` sem alteração de domínio ou estrutura.

---

## 📂 Estrutura de Arquivos

```
starsnin-browser/
├── index.html            # Interface principal do navegador com abas laterais
├── styles.css            # Estilização com design system do ecossistema Starsnin
├── starsnin-engine.js    # Motor de renderização próprio (Lexer, DOM Builder, Layout)
├── tab-manager.js        # Gerenciador de abas verticais laterais e histórico
├── server.js             # Servidor desktop leve (Zero dependências externas)
├── package.json          # Configuração de execução npm
└── README.md             # Documentação completa
```

---

## 🚀 Como Executar

### Opção 1: Via Node.js (Recomendado para navegação ao vivo com proxy CORS)

```bash
cd starsnin-browser
npm start
```
Abra `http://localhost:3000` em qualquer ambiente. O servidor inclui proxy leve nativo para contornar restrições de CORS ao buscar páginas externas ao vivo.

### Opção 2: Execução Direta (Sem servidor)
Basta abrir o arquivo `starsnin-browser/index.html` diretamente em qualquer navegador ou visualizador.

---

## 📱 Versão Android Nativa Integrada

O projeto também inclui a versão Android nativa completa em **Kotlin e Jetpack Compose** na pasta `app/`, construída com os mesmos princípios:
- Engine própria em Compose (`HtmlParser.kt` e `ComposeRenderer.kt`).
- Abas verticais na lateral (`VerticalTabsSidebar.kt`).
- Logo oficial carregada via Coil diretamente de `https://starsnin.vercel.app/starsninlogo.png`.
- Zero uso de `android.webkit.WebView`!
