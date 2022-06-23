import { useState } from 'react'
import { Box, Modal, TextField, Button } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import Navbar from './Components/NavBar'
import Compiler from './Pages/Compiler'
import Main from './Pages/Main'
import { blue } from '@mui/material/colors'
import InputAdornment from '@mui/material/InputAdornment';
import EmailIcon from '@mui/icons-material/Email';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import LockIcon from '@mui/icons-material/Lock';
import PersonIcon from '@mui/icons-material/Person';

function LoginPopUp({ loginPopUp, setLoginPopUp}) {
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
						label="Email"
						type="email"
						autoComplete="current-email"
						
						/>
				
					<TextField
					
						id="password"
						label="Password"
						type="password"
						autoComplete="current-password"
					
						
						/>
					  <Button  variant="contained" color="success">
						შესვლა
					</Button>
					
					</Box>
			</Box>
		</Modal>
	)
}

function RegisterPopUp({ registerPopUp, setRegisterPopUp}) {
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
				label="FirstName"
				type="FirstName"
				autoComplete="FirstName"
			
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
				label="LastName"
				type="LastName"
				autoComplete="LastName"
			
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
				label="Username"
				type="Nickname"
				autoComplete="nickname"
				
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
					label="Email"
					type="email"
					autoComplete="current-email"
				
				/>
		
			<TextField
			
				id="register-password"
				label="Password"
				type="password"
				autoComplete="current-password"
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<LockIcon></LockIcon>
					  </InputAdornment>
					)}}
				/>
				 <Button  variant="contained" color="success">
						რეგისტრაცია
					</Button>
			</Box>
			</Box>
		</Modal>
	)
}

function App() {

	return (
		<Box display="flex" flexDirection="column">
			<LoginPopUp loginPopUp={loginPopUp} setLoginPopUp={setLoginPopUp}
			 />
			<RegisterPopUp
				registerPopUp={registerPopUp}
				setRegisterPopUp={setRegisterPopUp}
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
			</Routes>
		</Box>
	)
}

export default App
