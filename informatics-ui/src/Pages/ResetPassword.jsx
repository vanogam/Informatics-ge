import { useState, useRef, useContext } from 'react'
import Box from '@mui/material/Box'
import InputLabel from '@mui/material/InputLabel'
import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import axios from 'axios'
import EmailIcon from '@mui/icons-material/Email'
import { Button } from '@mui/material'
import Typography from '@mui/material/Typography'
import logo from '../Components/logo.png'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import { toast } from 'react-toastify'
import { AuthContext } from '../store/authentication'
import { useNavigate } from 'react-router-dom'
import {
	Email,
	AccountCircle,
	Lock,
	Person,
} from '@mui/icons-material'

export default function ResetPassword() {
	const username = useRef('')
	const [success, setSuccess] = useState('')
	const authProvider = useContext(AuthContext)
	let navigate = useNavigate()
	if (authProvider.isLoggedIn) {
		return navigate('/')
	}
	const handleReset = () => {
		const body = {
			username: username.current.value,
		}
		axios
			.post('http://localhost:8080/recover/request', body)
			.then((response) => {
				if (response.data.status === 'SUCCESS') {
					setSuccess('True')
					toast.success('Recovery Link Sent')
				} else if (response.data.status === 'FAIL') {
					setSuccess('False')
					toast.error('Recovery Link Error')
				}
			})
			.catch((error) => {
				console.log(error)
				toast.error('Recovery Link: Unknown Error')
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
					<InputLabel>შეიყვანეთ მომხმარებლის სახელი:</InputLabel>
					<TextField sx ={{width:"90%", marginLeft:"20px"}}
						InputProps={{
							startAdornment: (
								<InputAdornment position="start">
									<AccountCircle></AccountCircle>
								</InputAdornment>
							),
							}}
						id="register-email"
						type="text"
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
					პაროლის აღდგენა
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
							ლინკი გადმოგზავნილია ელ-ფოსტაზე{' '}
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
							მომხმარებელი არ მოიძებნა{' '}
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
