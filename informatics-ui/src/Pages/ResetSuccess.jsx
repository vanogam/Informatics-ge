import * as React from 'react'
import Box from '@mui/material/Box'
import InputLabel from '@mui/material/InputLabel'
import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import logo from '../assets/logo.png'
import { Button } from '@mui/material'
import { useState, useRef, useContext, useEffect } from 'react'
import Typography from '@mui/material/Typography'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import LockIcon from '@mui/icons-material/Lock'
import { useParams } from 'react-router-dom'
import { toast } from 'react-toastify'
import { AuthContext } from '../store/authentication'
import { useNavigate } from 'react-router-dom'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import getMessage from '../Components/lang'
export default function ResetSuccess() {
	let { token } = useParams()
	const axiosInstance = useContext(AxiosContext)

	const password = useRef('')
	const confirmPassword = useRef('')
	const [success, setSuccess] = useState('')
	const authProvider = useContext(AuthContext)
	let navigate = useNavigate()
	useEffect(() => {
		if (authProvider.isLoggedIn) {
			navigate('/')
		}
	}, [authProvider.isLoggedIn, navigate])
	const handleReset = () => {
		if (password.current.value !== confirmPassword.current.value) {
			setSuccess('False')
			toast.error(getMessage('ka', 'passwordMismatch'))
			return
		}

		axiosInstance
			.post(`/recover/update-password/${token}`, {
				newPassword: password.current.value,
			}, { ignoreErrors: true })
			.then(() => {
				setSuccess('True')
				toast.success(getMessage('ka', 'passwordChanged'))
			})
			.catch((error) => {
				setSuccess('False')
				toast.error(getMessage('ka', error.response?.data?.message) || getMessage('ka', 'passwordResetFailed'))
			})
	}
	return (
		<Box sx={{ display: 'flex', flexDirection: 'row', marginLeft: '25%' }}>
			<Box>
				<Box
					sx={{ '& > :not(style)': { marginLeft: '25%', marginTop: '15%' } }}
				>
					<InputLabel>{getMessage('ka', 'enterNewPassword')}</InputLabel>
					<Box>
						<TextField
							label={getMessage('ka', 'password')}
							type="password"
							autoComplete="current-password"
							inputRef={password}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<LockIcon></LockIcon>
									</InputAdornment>
								),
							}}
						/>
					</Box>
					<Box>
						<TextField
							id="confirm-password"
							label={getMessage('ka', 'confirmPassword')}
							type="password"
							autoComplete="current-password"
							inputRef={confirmPassword}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<LockIcon></LockIcon>
									</InputAdornment>
								),
							}}
						/>
					</Box>
				</Box>

				<Button
					sx={{
						marginLeft: '30%',
						marginTop: '12%',
						background: 'rgb(42,13,56)',
					}}
					onClick={() => handleReset()}
					variant="contained"
				>
					{getMessage('ka', 'recoverPassword')}
				</Button>
				{success === 'True' && (
					<Box
						display="flex"
						justifyContent="row"
						sx={{ marginLeft: '30%', marginTop: '3%' }}
					>
						<CheckCircleIcon></CheckCircleIcon>
						<Typography
							gutterBottom
							variant="p"
							component="div"
							sx={{ color: 'green' }}
						>
							{getMessage('ka', 'passwordChanged')}{' '}
						</Typography>{' '}
					</Box>
				)}
				{success === 'False' && (
					<Box
						display="flex"
						justifyContent="row"
						sx={{ marginLeft: '30%', marginTop: '3%' }}
					>
						<CancelIcon></CancelIcon>
						<Typography
							gutterBottom
							variant="p"
							component="div"
							sx={{ color: 'red' }}
						>
							{getMessage('ka', 'passwordResetFailed')}{' '}
						</Typography>{' '}
					</Box>
				)}
			</Box>

			<Box sx={{ marginLeft: '5%', marginTop: '10%' }}>
				<img src={logo} height={'90%'} width={'50%'} />
			</Box>
		</Box>
	)
}
