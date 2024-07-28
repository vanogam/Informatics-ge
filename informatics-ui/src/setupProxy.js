const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
	app.use(
		'/api', // the endpoint you want to proxy
		createProxyMiddleware({
			target: 'http://localhost:8080', // the target API server
			changeOrigin: true,
		})
	);
};