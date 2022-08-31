import { useRef, useState, useContext } from 'react'
import { Button, Modal, TextField } from '@mui/material'
import axios from 'axios'
import { Box } from '@mui/system'
import { NavLink } from 'react-router-dom'
import { AuthContext } from '../store/authentication'
import { toast } from 'react-toastify'

export default function Login() {
	const [popUp, setPopUp] = useState(false)
	const [credentialsError, setCredentialsError] = useState(false)
	const nickname = useRef('')
	const password = useRef('')
	const authContext = useContext(AuthContext)

	const handleLoginResponse = async (response) => {
		if (response.data.status === 'SUCCESS') {
			axios.get('http://localhost:8080/get-user').then((res) => {
				let roles = res.data.roles
				setPopUp(false)
				toast.success('Login Success')
				setCredentialsError(false)
				authContext.login({ username: response.data.message, roles: roles })
			})
		} else if (response.data.status === 'FAIL') {
			setCredentialsError(true)
			toast.error('Login Error')
		}
	}

	function handlePassError() {
		setCredentialsError(false)
		setPopUp(false)
	}

	const handleLoginSubmit = () => {
		axios
			.post('http://localhost:8080/login', {
				username: nickname.current.value,
				password: password.current.value,
			})
			.then((response) => handleLoginResponse(response))
			.catch((error) => console.log(error))
	}

	return (
		<>
			<Button
				className="items"
				id="login"
				sx={{
					marginLeft: '10%',
					marginInline: '2px',
					alignSelf: 'flex-end',
					color: '#e1dce6',
				}}
				onClick={() => setPopUp(true)}
			>
				შესვლა
			</Button>
			<Modal open={popUp} onClose={() => setPopUp(false)}>
				<Box
					sx={{
						position: 'absolute',
						top: '50%',
						left: '50%',
						transform: 'translate(-50%, -50%)',
						width: '350px',
						bgcolor: 'white',
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
							id="nickname"
							label="მომხმარებელი"
							type="nickname"
							autoComplete="current-nickname"
							inputRef={nickname}
						/>

						<TextField
							id="password"
							label="პაროლი"
							type="password"
							autoComplete="current-password"
							inputRef={password}
						/>
						<Button
							sx={{ background: '#3c324e' , marginLeft: '5%', marginBottom: '15%'}}
							onClick={() => handleLoginSubmit()}
							variant="contained"
							color="success"
						>
							შესვლა
						</Button>

						{credentialsError && (
							<Button
								sx={{ color: 'red' }}
								component={NavLink}
								onClick={handlePassError}
								to="/reset"
							>
								მონაცემები არასწორია. დაგავიწყდა პაროლი?
							</Button>
						)}
					</Box>
				</Box>
			</Modal>
		</>
	)
}
