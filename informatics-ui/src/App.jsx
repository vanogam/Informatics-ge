import { useState } from 'react'
import { Box, Modal, TextField, Button, Typography } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import Navbar from './Components/NavBar'
import Compiler from './Pages/Compiler'
import Contests from './Pages/Contests'
import Main from './Pages/Main'
import { blue } from '@mui/material/colors'
import InputAdornment from '@mui/material/InputAdornment';
import EmailIcon from '@mui/icons-material/Email';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import LockIcon from '@mui/icons-material/Lock';
import PersonIcon from '@mui/icons-material/Person';
import axios from 'axios';
import { NavLink } from 'react-router-dom'
import ResetSuccess from "./Pages/ResetSuccess"
import ResetPassword from "./Pages/ResetPassword"
import Materials from './Pages/Materials'

function LoginPopUp({ setErrorMessage, loginPopUp, setRegisterPopUp, setLoginPopUp, email, password, handleInputChange, handleLoginSubmit, errorMessage }) {
	function handleEmailError(){
		setErrorMessage('')
		setLoginPopUp(false)
		setRegisterPopUp(true) 
	}
	function handlePassError(){
		setErrorMessage('')
		setLoginPopUp(false)
	}

	return (
		<Modal open={loginPopUp} onClose={() => setLoginPopUp(false)}>
			<Box
				sx={{
					position: 'absolute',
					top: '50%',
					left: '50%',
					transform: 'translate(-50%, -50%)',
					width: 400,
					bgcolor: 'background.paper',
					border: `2px solid ;`,
					borderRadius: '0.5rem',
					boxShadow: 24,
					p: 4,
				}}
			>
				   <Box
					component="form"
					sx={{
						'& .MuiTextField-root': { m: 1, width: '25ch' },
					}}
					noValidate
					autoComplete="off"
					>
						<TextField
						id="email"
						label="ელ-ფოსტა"
						type="email"
						autoComplete="current-email"
						value={email} onChange = {(e) => handleInputChange(e)} 
						/>
				
					<TextField
					
						id="password"
						label="პაროლი"
						type="password"
						autoComplete="current-password"
						// inputRef={textInput}
						value={password} 
						onChange = {(e) => handleInputChange(e)} 
						/>
					  <Button  sx = {{	background: '#3c324e',
					  }} onClick={()=>handleLoginSubmit()} variant="contained" color="success">
						შესვლა
					</Button>
				

					{errorMessage==='P' && (
					<Button
							sx={{color: 'red'}}
							component={NavLink}
							onClick={()=>handlePassError()} 
							to="/reset"
						>
						პაროლი არასწორია. დაგავიწყდა პაროლი? 
						</Button>
					)}

					{errorMessage==='E' && (
										<Button 
												sx={{color: 'red'}}
												component={NavLink}
												onClick={()=>handleEmailError()} 
												to="/"
											>
											მომხარებელი არ მოიძებნა. რეგისტრაცია 
											</Button>
										)}
										
				</Box>
			</Box>
		</Modal>
	)
}

function RegisterPopUp({success, confirmPassword, registerPopUp, setRegisterPopUp, registerEmail, registerFirstName, registerLastName, registerPassword, registerUsername, handleInputChange, handleRegistrationSubmit }) {
	
	return (
		<Modal open={registerPopUp} onClose={() => setRegisterPopUp(false)}>
			<Box
				sx={{
					position: 'absolute',
					top: '50%',
					left: '50%',
					transform: 'translate(-50%, -50%)',
					width: 400,
					bgcolor: 'background.paper',
					border: `2px solid ${blue[700]}`,
					borderRadius: '0.5rem',
					boxShadow: 24,
					p: 4,
				}}
			>  <Box
			component="form"
			sx={{
				'& .MuiTextField-root': { m: 1, width: '30ch' },
			}}
			noValidate
			autoComplete="off"
			>	<TextField
				style = {{ alignItems: 'center',  justifyContent: 'center'}}
				id="register-first-name"
				label="სახელი"
				type="FirstName"
				autoComplete="FirstName"
				value={registerFirstName} 
				onChange = {(e) => handleInputChange(e)} 
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<PersonIcon></PersonIcon>
					  </InputAdornment>
					)}}
				/>
				<TextField
				style = {{ alignItems: 'center',  justifyContent: 'center'}}
				id="register-last-name"
				label="გვარი"
				type="LastName"
				autoComplete="LastName"
				value={registerLastName} 
				onChange = {(e) => handleInputChange(e)} 
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<PersonIcon></PersonIcon>
					  </InputAdornment>
					)}}
				/>
				<TextField
				style = {{ alignItems: 'center',  justifyContent: 'center'}}
				id="username"
				label="username"
				type="Nickname"
				autoComplete="nickname"
				value={registerUsername} 
				onChange = {(e) => handleInputChange(e)} 
				InputProps={{
					startAdornment: (
					<InputAdornment position="start">
						<AccountCircleIcon></AccountCircleIcon>
					</InputAdornment>
					)}}
				/>
				<TextField
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<EmailIcon></EmailIcon>
					  </InputAdornment>
					)}}
					id="register-email"
					label="ელ-ფოსტა"
					type="email"
					autoComplete="current-email"
					value={registerEmail} 
					onChange = {(e) => handleInputChange(e)} 
				/>
		
			<TextField
			
				id="register-password"
				label="პაროლი"
				type="password"
				autoComplete="current-password"
				value={registerPassword} 
				onChange = {(e) => handleInputChange(e)} 
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<LockIcon></LockIcon>
					  </InputAdornment>
					)}}
				/>
				 {/* <Button sx = {{	background: 'rgb(42,13,56)',
									background: 'linear-gradient(90deg, rgba(42,13,56,1) 63%, rgba(53,26,88,1) 77%, rgba(73,62,153,1) 92%)'}}onClick={()=>handleRegistrationSubmit()} variant="contained">
						რეგისტრაცია
					</Button> */}
			   <Box>
				<TextField
				id="confirm-password"
						label="გაიმეორე პაროლი"
						type="password"
						autoComplete="current-password"
						value={confirmPassword} 
						onChange = {(e) => handleInputChange(e)} 
						InputProps={{
							startAdornment: (
							<InputAdornment position="start">
								<LockIcon></LockIcon>
							</InputAdornment>
							)}}
						/>
        </Box>
			
		{success==="False" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '10%', marginTop:'3%'}}>
				<Typography gutterBottom variant="p" component="div" sx = {{color: 'red'}}>
		პაროლები არ ემთხვევა ერთმანეთს </Typography> </Box>
		
		
				)}
		{/* {success==="True" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '10%', marginTop:'3%'}}>
				<Typography gutterBottom variant="p" component="div" sx = {{color: 'green'}}>
		თქვენ წარმატებით დარეგისტრირდით </Typography> </Box> )} */}
			</Box>
			<Button sx = {{	background: '#3c324e', marginLeft: '2%', marginTop: '2%',
									}}onClick={()=>handleRegistrationSubmit()} variant="contained">
						რეგისტრაცია
					</Button>
			</Box>
		</Modal>
	)
}

function App() {
	const [isLogin, setIsLogin] = useState(false)
	const [loginPopUp, setLoginPopUp] = useState(false)
	const [registerPopUp, setRegisterPopUp] = useState(false)
	const [email, setEmail] = useState('');
    const [password,setPassword] = useState('')
	const [errorMessage, setErrorMessage] = useState('');
	
	const [registerEmail, setRegisterEmail] = useState('');
    const [registerPassword,setRegisterPassword] = useState('')
	const [registerFirstName,setRegisterFirstName] = useState('')
	const [registerLastName,setRegisterLastName] = useState('')
	const [registerUsername,setRegisterUsername] = useState('')
	const [confirmPassword, setConfirmPassword] = useState('');
	const [success, setSuccess] = useState('');

	const handleInputChange = (e) => {
		console.log(e)
		const {id , value} = e.target;
		
		if(id === "email"){
			setEmail(value);
		}
		if(id === "password"){
		
			setPassword(value);
		}
		if(id === 'register-email'){
			setRegisterEmail(value)
		}

		if(id === 'register-password'){
			setRegisterPassword(value)
		}

		if(id === 'register-first-name'){
			setRegisterFirstName(value)
		}

		if(id === 'register-last-name'){
			setRegisterLastName(value)
		}

		if(id === 'username'){
			setRegisterUsername(value)
		}
		
		if(id === 'confirm-password'){
			setConfirmPassword(value)
		}
		
	
	}
	
	const changeAuthStatus = (response) => {
	
		if (response.data.status === "SUCCESS"){
			console.log("Logging in")
			setLoginPopUp(false)
			setIsLogin(true)
			
		}
		else if (response.data.status === "FAIL"){
				console.log("Login Fail")
			
					setErrorMessage('P')
			}
		

			
	}
	const handleLoginSubmit  = () => {
		setErrorMessage('')
		// setEmail(email)
		// setPassword(password)
		console.log(email, password)
		// if (email === 'a'){
		// 	setErrorMessage('E')
		// }
		if (password === 'a'){
			setErrorMessage('P')
		}
		const body = {'password': password, 'username' : email}
		axios.post('http://localhost:8080/login', body )
			.then(response => changeAuthStatus(response));
	
	}

	const handleRegistrationSubmit = () => {
		setSuccess("True")
		if (registerPassword === confirmPassword) {
			setSuccess("True")
			const body = {'username': registerUsername, 'firstName': registerFirstName, 'lastName': registerLastName, 'password': registerPassword}
			// const body = {'username':registerUsername, 'password':registerPassword}
			console.log(body)
			console.log(registerEmail, registerFirstName, registerLastName, registerPassword, registerUsername)
			axios.post('http://localhost:8080/register', body).then(response => console.log(response));
			
			setRegisterPopUp(false)
			const login_body = {'password':registerPassword, 'username' : registerUsername}
			axios.post('http://localhost:8080/login', login_body )
				.then(response => changeAuthStatus(response));
			
		}
		
		else{
			setSuccess("False")
		}
		
	}

	return (
		<div className="App">
		<Box display="flex" flexDirection="column">
			<LoginPopUp setRegisterPopUp = {setRegisterPopUp} setErrorMessage={setErrorMessage} loginPopUp={loginPopUp} setLoginPopUp={setLoginPopUp}
			email={email} handleInputChange={handleInputChange} password={password} handleLoginSubmit={handleLoginSubmit} errorMessage={errorMessage} />
			<RegisterPopUp
			    setConfirmPassword = {setConfirmPassword}
				registerPopUp={registerPopUp}
				success = {success}
				confirmPassword = {confirmPassword}
				setRegisterPopUp={setRegisterPopUp} registerEmail={registerEmail} registerFirstName={registerFirstName}
				registerLastName={registerLastName} registerPassword={registerPassword} registerUsername={registerUsername}
				handleInputChange={handleInputChange} handleRegistrationSubmit={handleRegistrationSubmit}
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