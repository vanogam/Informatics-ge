import {
	Button,
	Modal,
	TextField,
	Typography,
} from '@mui/material'
import { Box } from '@mui/system'
import InputAdornment from '@mui/material/InputAdornment';
import { blue } from '@mui/material/colors'

import {
	Email,
	AccountCircle,
	Lock,
	Person,
} from '@mui/icons-material'

function RegisterPopUp({
	success,
	confirmPassword,
	registerPopUp,
	setRegisterPopUp,
	registerEmail,
	registerFirstName,
	registerLastName,
	registerPassword,
	registerUsername,
	handleInputChange,
	handleLoginPopUpClose,
	handleRegistrationSubmit,
}) {
	return (
		<Modal
			open={registerPopUp}
			onClose={() => {
				setRegisterPopUp(false)
				handleLoginPopUpClose()
			}}
		>
			<Box
				sx={{
					position: 'absolute',
					top: '50%',
					left: '50%',
					transform: 'translate(-50%, -50%)',
					width: '300px',
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
						'& .MuiTextField-root': { m: 1, width: '20ch' },
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
						value={registerFirstName}
						onChange={(e) => handleInputChange(e)}
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
						value={registerLastName}
						onChange={(e) => handleInputChange(e)}
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
						value={registerUsername}
						onChange={(e) => handleInputChange(e)}
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
						value={registerEmail}
						onChange={(e) => handleInputChange(e)}
					/>
					<TextField
						id="register-password"
						label="პაროლი"
						type="password"
						autoComplete="current-password"
						value={registerPassword}
						onChange={(e) => handleInputChange(e)}
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
							value={confirmPassword}
							onChange={(e) => handleInputChange(e)}
							InputProps={{
								startAdornment: (
									<InputAdornment position="start">
										<Lock></Lock>
									</InputAdornment>
								),
							}}
						/>
					</Box>
					{success === 'False' && (
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
	)
}

export default RegisterPopUp
