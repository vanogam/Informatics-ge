import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import { AuthContextProvider } from './store/authentication'
import App from './App'
import './index.css'
import 'react-toastify/dist/ReactToastify.css'

const root = ReactDOM.createRoot(document.getElementById('root'))
root.render(
	<React.StrictMode>
		<AuthContextProvider>
			<BrowserRouter>
				<ToastContainer />
				<App />
			</BrowserRouter>
		</AuthContextProvider>
	</React.StrictMode>
)
