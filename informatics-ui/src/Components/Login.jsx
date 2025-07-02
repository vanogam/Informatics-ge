import { useRef, useState, useContext } from 'react'
import { Button, Modal, TextField } from '@mui/material'
import { Box } from '@mui/system'
import { NavLink } from 'react-router-dom'
import { AuthContext } from '../store/authentication'
import { toast } from 'react-toastify'
import { AxiosContext } from '../utils/axiosInstance'
import getMessage from "./lang";

export default function Login() {
	const [popUp, setPopUp] = useState(false)
	const [credentialsError, setCredentialsError] = useState(false)
	const username = useRef('')
	const password = useRef('')
	const authContext = useContext(AuthContext)
	const axiosInstance = useContext(AxiosContext)

	const handleLoginResponse = async (response) => {
		axiosInstance.get('/user').then((res) => {
			let role = res.data.role
			setPopUp(false)
			toast.success(getMessage('ka', 'loginSuccess'))
			setCredentialsError(false)
			authContext.login({ username: response.data.username, role: role })
		})
	}

	function handlePassError() {
		setCredentialsError(false)
		setPopUp(false)
	}

	const handleLoginSubmit = () => {
		axiosInstance
			.post('/login', {
				username: username.current.value,
				password: password.current.value,
			},
				{ withCredentials: true })
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
							id="username"
							label="მომხმარებელი"
							type="username"
							autoComplete="current-nickname"
							inputRef={username}
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
