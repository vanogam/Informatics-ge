import { Button, Modal, TextField } from "@mui/material"
import { Box } from "@mui/system"
import { NavLink } from 'react-router-dom'

function LoginPopUp({
	setErrorMessage,
	loginPopUp,
	setRegisterPopUp,
	setLoginPopUp,
	email,
	password,
	handleInputChange,
	handleLoginPopUpClose,
	handleLoginSubmit,
	errorMessage,
}) {
	function handleEmailError() {
		setErrorMessage('')
		setLoginPopUp(false)
		setRegisterPopUp(true)
	}
	function handlePassError() {
		setErrorMessage('')
		setLoginPopUp(false)
	}

	return (
		<Modal
			open={loginPopUp}
			onClose={() => {
				setLoginPopUp(false)
				handleLoginPopUpClose()
			}}
		>
			<Box
				sx={{
					position: 'absolute',
					top: '50%',
					left: '50%',
					transform: 'translate(-50%, -50%)',
					width: 400,
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
						id="email"
						label="ელ-ფოსტა"
						type="email"
						autoComplete="current-email"
						value={email}
						onChange={(e) => handleInputChange(e)}
					/>

					<TextField
						id="password"
						label="პაროლი"
						type="password"
						autoComplete="current-password"
						// inputRef={textInput}
						value={password}
						onChange={(e) => handleInputChange(e)}
					/>
					<Button
						sx={{ background: '#3c324e' }}
						onClick={() => handleLoginSubmit()}
						variant="contained"
						color="success"
					>
						შესვლა
					</Button>

					{errorMessage === 'P' && (
						<Button
							sx={{ color: 'red' }}
							component={NavLink}
							onClick={() => handlePassError()}
							to="/reset"
						>
							პაროლი არასწორია. დაგავიწყდა პაროლი?
						</Button>
					)}

					{errorMessage === 'E' && (
						<Button
							sx={{ color: 'red' }}
							component={NavLink}
							onClick={() => handleEmailError()}
							to="/"
						>
							მომხარებელი არ მოიძებნა. რეგისტრაცია
						</Button>
					)}
				</Box>
			</Box>
		</Modal>
	)
}

export default LoginPopUp