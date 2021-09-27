const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/earth',
    createProxyMiddleware({
        target: 'https://localhost:12112',
        pathRewrite: {'^/earth/api' : '/api'},
        changeOrigin: true,
        secure: false,
    })
  );
      app.use(
    '/mars',
    createProxyMiddleware({
        target: 'https://localhost:12116',
        pathRewrite: {'^/mars/api' : '/api'},
        changeOrigin: true,
        secure: false,
    })
      );
    app.use(
    '/pluto',
    createProxyMiddleware({
        target: 'https://localhost:12119',
        pathRewrite: {'^/pluto/api' : '/api'},
        changeOrigin: true,
        secure: false,
    })
  );
    app.listen(3000);
};