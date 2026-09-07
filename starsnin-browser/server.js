/**
 * Servidor Desktop Leve Starsnin
 * Usa apenas módulos nativos do Node.js (http, https, url, fs, path)
 * Zero dependências externas!
 */

const http = require('http');
const https = require('https');
const url = require('url');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);

  // Endpoint de Proxy para contornar CORS e alimentar a Engine Independente
  if (parsedUrl.pathname === '/api/fetch') {
    const targetUrl = parsedUrl.query.url;
    if (!targetUrl) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      return res.end(JSON.stringify({ error: 'Parâmetro url é obrigatório' }));
    }

    try {
      const parsedTarget = new URL(targetUrl);
      const client = parsedTarget.protocol === 'https:' ? https : http;

      const fetchReq = client.get(targetUrl, {
        headers: {
          'User-Agent': 'StarsninEngine/1.0 (Independent Custom Engine; No-Chromium)',
          'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
        }
      }, (fetchRes) => {
        // Redirecionamento
        if ([301, 302, 307, 308].includes(fetchRes.statusCode) && fetchRes.headers.location) {
          const redirectUrl = new URL(fetchRes.headers.location, targetUrl).toString();
          res.writeHead(302, { 'Location': `/api/fetch?url=${encodeURIComponent(redirectUrl)}` });
          return res.end();
        }

        res.writeHead(fetchRes.statusCode, {
          'Content-Type': fetchRes.headers['content-type'] || 'text/html; charset=utf-8',
          'Access-Control-Allow-Origin': '*'
        });
        fetchRes.pipe(res);
      });

      fetchReq.on('error', (err) => {
        res.writeHead(502, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Erro de conexão: ' + err.message }));
      });
    } catch (e) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'URL inválida' }));
    }
    return;
  }

  // Servidor de arquivos estáticos da interface do navegador
  let filePath = path.join(__dirname, parsedUrl.pathname === '/' ? 'index.html' : parsedUrl.pathname);
  const ext = path.extname(filePath).toLowerCase();

  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('404 Não Encontrado');
      } else {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('Erro interno do servidor');
      }
    } else {
      res.writeHead(200, {
        'Content-Type': MIME_TYPES[ext] || 'application/octet-stream',
        'Cache-Control': 'no-cache'
      });
      res.end(content);
    }
  });
});

server.listen(PORT, () => {
  console.log(`====================================================`);
  console.log(`🚀 Starsnin Browser em execução!`);
  console.log(`📍 Acesse no navegador: http://localhost:${PORT}`);
  console.log(`🛡️  Garantia: Zero Chromium / Sem WebViews nativas`);
  console.log(`📐 Abas: Formato vertical no canto lateral`);
  console.log(`⭐ Destino: https://starsnin.vercel.app`);
  console.log(`====================================================`);
});
