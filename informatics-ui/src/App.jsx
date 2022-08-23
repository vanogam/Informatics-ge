import { useState } from 'react'
import { Box, Modal, TextField, Button } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import Navbar from './Components/NavBar'
import Compiler from './Pages/Compiler'
import Contests from './Pages/Contests'
import Main from './Pages/Main'
import axios from 'axios'
import ResetSuccess from './Pages/ResetSuccess'
import ResetPassword from './Pages/ResetPassword'
import Materials from './Pages/Materials'
import { toast } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import RegisterPopUp from './Components/RegisterPopUp'
import LoginPopUp from './Components/LoginPopUp'

function App() {
	const [isLogin, setIsLogin] = useState(false)
	const [loginPopUp, setLoginPopUp] = useState(false)
	const [registerPopUp, setRegisterPopUp] = useState(false)
	const [email, setEmail] = useState('')
	const [password, setPassword] = useState('')
	const [errorMessage, setErrorMessage] = useState('')

	const [registerEmail, setRegisterEmail] = useState('')
	const [registerPassword, setRegisterPassword] = useState('')
	const [registerFirstName, setRegisterFirstName] = useState('')
	const [registerLastName, setRegisterLastName] = useState('')
	const [registerUsername, setRegisterUsername] = useState('')
	const [confirmPassword, setConfirmPassword] = useState('')
	const [success, setSuccess] = useState('')

	const handleLoginPopUpClose = () => {
		setEmail("")
		setPassword("")
		setRegisterEmail("")
		setRegisterPassword("")
		setRegisterFirstName("")
		setRegisterLastName("")
		setRegisterUsername("")
		setConfirmPassword("")
	}
	const handleInputChange = (e) => {
		console.log(e)
		const { id, value } = e.target

		if (id === 'email') {
			setEmail(value)
		}
		if (id === 'password') {
			setPassword(value)
		}
		if (id === 'register-email') {
			setRegisterEmail(value)
		}

		if (id === 'register-password') {
			setRegisterPassword(value)
		}

		if (id === 'register-first-name') {
			setRegisterFirstName(value)
		}

		if (id === 'register-last-name') {
			setRegisterLastName(value)
		}

		if (id === 'username') {
			setRegisterUsername(value)
		}

		if (id === 'confirm-password') {
			setConfirmPassword(value)
		}
	}

	const changeAuthStatus = (response) => {
		if (response.data.status === 'SUCCESS') {
			console.log('Logging in')
			setLoginPopUp(false)
			setIsLogin(true)
		} else if (response.data.status === 'FAIL') {
			console.log('Login Fail')
			toast.error('Hello Geeks')
			setErrorMessage('P')
		}
	}

	const handleLoginSubmit = () => {
		setErrorMessage('')
		// setEmail(email)
		// setPassword(password)
		console.log(email, password)
		// if (email === 'a'){
		// 	setErrorMessage('E')
		// }
		// if (password === 'a'){
		// 	setErrorMessage('P')
		// }
		const body = { password: password, username: email }
		axios
			.post('http://localhost:8080/login', body)
			.then((response) => changeAuthStatus(response))
	}

	const handleRegistrationSubmit = () => {
		setSuccess('True')
		if (registerPassword === confirmPassword) {
			setSuccess('True')
			const body = {
				username: registerUsername,
				firstName: registerFirstName,
				lastName: registerLastName,
				password: registerPassword,
			}
			// const body = {'username':registerUsername, 'password':registerPassword}
			console.log(body)
			console.log(
				registerEmail,
				registerFirstName,
				registerLastName,
				registerPassword,
				registerUsername
			)
			axios
				.post('http://localhost:8080/register', body)
				.then((response) => console.log(response))

			setRegisterPopUp(false)
			const login_body = {
				password: registerPassword,
				username: registerUsername,
			}
			axios
				.post('http://localhost:8080/login', login_body)
				.then((response) => changeAuthStatus(response))
		} else {
			setSuccess('False')
		}
	}

	return (
		<div className="App">
			<Box display="flex" flexDirection="column">
				<LoginPopUp
					setRegisterPopUp={setRegisterPopUp}
					setErrorMessage={setErrorMessage}
					loginPopUp={loginPopUp}
					setLoginPopUp={setLoginPopUp}
					email={email}
					handleInputChange={handleInputChange}
					handleLoginPopUpClose={handleLoginPopUpClose}
					password={password}
					handleLoginSubmit={handleLoginSubmit}
					errorMessage={errorMessage}
				/>
				<RegisterPopUp
					setConfirmPassword={setConfirmPassword}
					registerPopUp={registerPopUp}
					success={success}
					confirmPassword={confirmPassword}
					setRegisterPopUp={setRegisterPopUp}
					registerEmail={registerEmail}
					registerFirstName={registerFirstName}
					registerLastName={registerLastName}
					registerPassword={registerPassword}
					registerUsername={registerUsername}
					handleInputChange={handleInputChange}
					handleLoginPopUpClose={handleLoginPopUpClose}
					handleRegistrationSubmit={handleRegistrationSubmit}
				/>
				<Navbar
					isLogin={isLogin}
					setIsLogin={setIsLogin}
					setLoginPopUp={setLoginPopUp}
					setRegisterPopUp={setRegisterPopUp}
				/>

				<Routes>
					<Route path="/" element={<Main />} />
					<Route path="/compiler" element={<Compiler />} />
					<Route path="/reset" element={<ResetPassword />} />
					<Route path="/resetSuccess" element={<ResetSuccess />} />
					<Route path="/materials" element={<Materials />} />
					<Route path="/contests" element={<Contests />} />
				</Routes>
			</Box>
		</div>
	)
}

export default App
// #mvn clean install
// #sudo docker-compose up --build
