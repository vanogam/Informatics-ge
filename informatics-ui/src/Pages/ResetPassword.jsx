import { useState, useRef, useContext, useEffect } from 'react'
import Box from '@mui/material/Box'
import InputLabel from '@mui/material/InputLabel'
import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import { Button } from '@mui/material'
import Typography from '@mui/material/Typography'
import logo from '../assets/logo.png'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import { toast } from 'react-toastify'
import { AuthContext } from '../store/authentication'
import { useNavigate } from 'react-router-dom'
import {
	AccountCircle,
} from '@mui/icons-material'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import getMessage from '../Components/lang'

export default function ResetPassword() {
	const axiosInstance = useContext(AxiosContext)
	const username = useRef('')
	const [success, setSuccess] = useState('')
	const authProvider = useContext(AuthContext)
	let navigate = useNavigate()
	useEffect(() => {
		if (authProvider.isLoggedIn) {
			navigate('/')
		}
	}, [authProvider.isLoggedIn, navigate])
	const handleReset = () => {
		const body = {
			username: username.current.value,
		}
		axiosInstance
			.post(`/recover/request`, body, { ignoreErrors: true })
			.then(() => {
				setSuccess('True')
				toast.success(getMessage('ka', 'recoveryLinkSent'))
			})
			.catch((error) => {
				setSuccess('False')
				toast.error(getMessage('ka', error.response?.data?.message) || getMessage('ka', 'recoveryRequestFailed'))
			})
	}
	return (
		<Box
			sx={{
				display: 'flex',
				width: '50%',
				height: '100%',
				justifyContent: 'center',
				alignItems: 'center',
				flexDirection: 'row',
				marginInline: 'auto',
				marginTop: '3rem'
			}}
		>
			<Box>
				<Box sx={{marginLeft:"20px"}}>
					<InputLabel>{getMessage('ka', 'enterUsernameLabel')}</InputLabel>
					<TextField sx ={{width:"90%", marginLeft:"20px"}}
						InputProps={{
							startAdornment: (
								<InputAdornment position="start">
									<AccountCircle></AccountCircle>
								</InputAdornment>
							),
							}}
						id="register-email"
						type="codeText"
						autoComplete="current-username"
						inputRef={username}
					/>
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
						sx={{ marginLeft: '30%', marginTop: '12%' }}
					>
						<CheckCircleIcon></CheckCircleIcon>
						<Typography
							gutterBottom
							variant="p"
							component="div"
							sx={{ color: 'green', fontSize: '0.8rem' }}
						>
							{getMessage('ka', 'recoveryLinkSent')}{' '}
						</Typography>{' '}
					</Box>
				)}
				{success === 'False' && (
					<Box
						display="flex"
						justifyContent="row"
						sx={{ marginLeft: '30%', marginTop: '12%' }}
					>
						<CancelIcon></CancelIcon>
						<Typography
							gutterBottom
							variant="p"
							component="div"
							sx={{ color: 'red', fontSize: '0.8rem' }}
						>
							{getMessage('ka', 'invalidUsername')}{' '}
						</Typography>{' '}
					</Box>
				)}
			</Box>

			<Box>
				<img src={logo} style={{ maxWidth: '20rem' }} />
			</Box>
		</Box>
	)
}
