import * as React from 'react'
import Box from '@mui/material/Box'
import InputLabel from '@mui/material/InputLabel'
import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import logo from '../assets/logo.png'
import { Button } from '@mui/material'
import { useState, useRef, useContext } from 'react'
import Typography from '@mui/material/Typography'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import LockIcon from '@mui/icons-material/Lock'
import { useParams } from 'react-router-dom'
import axios from 'axios'
import { toast } from 'react-toastify'
import { AuthContext } from '../store/authentication'
import { useNavigate } from 'react-router-dom'
export default function ResetSuccess() {
	let { token } = useParams()

	const password = useRef('')
	const confirmPassword = useRef('')
	const [success, setSuccess] = useState('')
	const authProvider = useContext(AuthContext)
	let navigate = useNavigate()
	if (authProvider.isLoggedIn) {
		return navigate('/')
	}
	const handleReset = () => {
		if (password.current.value !== confirmPassword.current.value) {
			return
			// setSuccess('True')
		}

		axios
			.post(`http://localhost:8080/recover/update-password/${token}`, {
				newPassword: password.current.value,
			})
			.then((response) => {
				if (response.data.status === 'SUCCESS') {
					toast.success("Password Changed Successfully")
				}else{
					toast.error("Error while changing password")
				}
			})
	}
	return (
		<Box sx={{ display: 'flex', flexDirection: 'row', marginLeft: '25%' }}>
			<Box>
				<Box
					sx={{ '& > :not(style)': { marginLeft: '25%', marginTop: '15%' } }}
				>
					<InputLabel>შეიყვანე ახალი პაროლი</InputLabel>
					{/* <Typography gutterBottom variant="p" component="div">
          შეიყვანე ელ-ფოსტა: </Typography> */}
					<Box>
						<TextField
							label="პაროლი"
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
							label="გაიმეორე პაროლი"
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
					პაროლის აღდგენა
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
							პაროლი შეიცვალა{' '}
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
							პაროლები არ ემთხვევა{' '}
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
