import { Button, Modal, TextField, Typography } from '@mui/material'
import { Box } from '@mui/system'
import InputAdornment from '@mui/material/InputAdornment'
import { blue } from '@mui/material/colors'

import {
	Email,
	AccountCircle,
	Lock,
	Person,
} from '@mui/icons-material'
import { useRef, useState, useContext } from 'react'
import { AuthContext } from '../store/authentication'
import axios from 'axios'

export default function Register() {
	const [popUp, setPopUp] = useState(false)
	const [passwordMatch, setPasswordMatch] = useState(true)
	const firstName = useRef('')
	const lastName = useRef('')
	const nickname = useRef('')
	const email = useRef('')
	const rePassword = useRef('')
	const password = useRef('')
	const authContext = useContext(AuthContext)

	const handleRegistrationSubmit = () => {
		if (password.current.value !== rePassword.current.value) {
			setPasswordMatch(false)
			return
		}
		setPasswordMatch(true)
		const body = {
			username: nickname.current.value,
			firstName: firstName.current.value,
			lastName: firstName.current.value,
			password: password.current.value,
		}

		axios
			.post('http://localhost:8080/register', body)
			.then((response) => {
				setPopUp(false)
				axios
					.post('http://localhost:8080/login', {
						username: nickname.current.value,
						password: password.current.value,
					})
					.then((response) => {
						if (response.data.status === "SUCCESS") {
							authContext.login(nickname.current.value)
						} else {
							console.error(response.data)
						}
					})
					.catch((error) => {
						console.error(error)
					})
			})
			.catch((error) => {
				console.error(error)
			})
	}

	return (
		<>
			<Button
				className="items"
				sx={{
					marginInline: '2px',
					alignSelf: 'flex-end',
					color: '#e1dce6',
				}}
				onClick={() => setPopUp(true)}
			>
				რეგისტრაცია
			</Button>
			<Modal open={popUp} onClose={() => setPopUp(false)}>
				<Box
					sx={{
						position: 'absolute',
						top: '50%',
						left: '50%',
						transform: 'translate(-50%, -50%)',
						width: 400,
						bgcolor: 'white',
						border: `2px solid ${blue[700]}`,
						borderRadius: '0.5rem',
						boxShadow: 24,
						p: 4,
					}}
				>
					{' '}
					<Box
						component="form"
						sx={{
							'& .MuiTextField-root': { m: 1, width: '30ch' },
						}}
						noValidate
						autoComplete="off"
					>
						{' '}
						<TextField
							style={{ alignItems: 'center', justifyContent: 'center' }}
							id="register-first-name"
							label="სახელი"
							type="FirstName"
							autoComplete="FirstName"
							inputRef={firstName}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<Person></Person>
									</InputAdornment>
								),
							}}
						/>
						<TextField
							style={{ alignItems: 'center', justifyContent: 'center' }}
							id="register-last-name"
							label="გვარი"
							type="LastName"
							autoComplete="LastName"
							inputRef={lastName}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<Person></Person>
									</InputAdornment>
								),
							}}
						/>
						<TextField
							style={{ alignItems: 'center', justifyContent: 'center' }}
							id="username"
							label="username"
							type="Nickname"
							autoComplete="nickname"
							inputRef={nickname}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<AccountCircle></AccountCircle>
									</InputAdornment>
								),
							}}
						/>
						<TextField
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<Email></Email>
									</InputAdornment>
								),
							}}
							id="register-email"
							label="ელ-ფოსტა"
							type="email"
							autoComplete="current-email"
							inputRef={email}
						/>
						<TextField
							id="register-password"
							label="პაროლი"
							type="password"
							autoComplete="current-password"
							inputRef={password}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<Lock></Lock>
									</InputAdornment>
								),
							}}
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
								inputRef={rePassword}
								InputProps={{
									startAdornment: (
										<InputAdornment position="start">
											<Lock></Lock>
										</InputAdornment>
									),
								}}
							/>
						</Box>
						{!passwordMatch && (
							<Box
								display="flex"
								justifyContent="row"
								sx={{ marginLeft: '10%', marginTop: '3%' }}
							>
								<Typography
									gutterBottom
									variant="p"
									component="div"
									sx={{ color: 'red' }}
								>
									პაროლები არ ემთხვევა ერთმანეთს{' '}
								</Typography>{' '}
							</Box>
						)}
						{/* {success==="True" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '10%', marginTop:'3%'}}>
				<Typography gutterBottom variant="p" component="div" sx = {{color: 'green'}}>
		თქვენ წარმატებით დარეგისტრირდით </Typography> </Box> )} */}
					</Box>
					<Button
						sx={{ background: '#3c324e', marginLeft: '2%', marginTop: '2%' }}
						onClick={() => handleRegistrationSubmit()}
						variant="contained"
					>
						რეგისტრაცია
					</Button>
				</Box>
			</Modal>
		</>
	)
}
