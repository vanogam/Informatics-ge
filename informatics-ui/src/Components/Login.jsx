import { useRef, useState, useContext } from 'react'
import { Button, Modal, TextField, FormControlLabel, Checkbox } from '@mui/material'
import { Box } from '@mui/system'
import { NavLink } from 'react-router-dom'
import { AuthContext } from '../store/authentication'
import { toast } from 'react-toastify'
import { AxiosContext } from '../utils/axiosInstance'
import getMessage from "./lang";

export default function Login() {
	const [popUp, setPopUp] = useState(false)
	const [credentialsError, setCredentialsError] = useState(false)
	const [rememberMe, setRememberMe] = useState(false)
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
				rememberMe: rememberMe,
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
						width: 'min(100% - 32px, 400px)',
						maxWidth: 400,
						bgcolor: 'white',
						border: '2px solid #3c324e',
						borderRadius: '0.5rem',
						boxShadow: 24,
						p: 3,
					}}
				>
					<Box
						component="form"
						onSubmit={(e) => {
							e.preventDefault()
							handleLoginSubmit()
						}}
						sx={{
							display: 'flex',
							flexDirection: 'column',
							gap: 2,
							width: '100%',
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
							fullWidth
							size="small"
						/>

						<TextField
							id="password"
							label="პაროლი"
							type="password"
							autoComplete="current-password"
							inputRef={password}
							fullWidth
							size="small"
						/>
						<FormControlLabel
							control={
								<Checkbox
									checked={rememberMe}
									onChange={(e) => setRememberMe(e.target.checked)}
									color="primary"
									size="small"
								/>
							}
							label="დამახსოვრება"
							sx={{ m: 0, alignSelf: 'flex-start', userSelect: 'none' }}
						/>

						<Button
							type="submit"
							fullWidth
							variant="contained"
							color="success"
							sx={{
								mt: 0.5,
								py: 1,
								backgroundColor: '#3c324e',
								'&:hover': { backgroundColor: '#2d253c' },
							}}
						>
							შესვლა
						</Button>

						{credentialsError && (
							<Button
								sx={{ color: 'red', alignSelf: 'center' }}
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
