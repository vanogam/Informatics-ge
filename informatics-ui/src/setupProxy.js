const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
	const enableSSL = process.env.ENABLE_SSL === 'true';
	const protocol = enableSSL ? 'https' : 'http';
	const host = process.env.REACT_APP_API_HOST || 'localhost';
	const port = process.env.REACT_APP_API_PORT || '8080';
	app.use(
		'/api',
		createProxyMiddleware({
			target: `${protocol}://${host}:${port}`,
			changeOrigin: true,
			onProxyReq: (proxyReq, req) => {
				console.log(`Proxying request: ${req.method} ${req.originalUrl} -> ${proxyReq.protocol}//${proxyReq.host}${proxyReq.path}`);
			},
			onProxyRes: (proxyRes, req, res) => {
				console.log(`Response from target: ${proxyRes.statusCode} for ${req.method} ${req.originalUrl}`);
			},
		})
	);
};