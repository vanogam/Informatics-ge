import {  Box, Button, Container, Link } from '@mui/material'
import { blue, indigo } from '@mui/material/colors'
import { NavLink } from 'react-router-dom'
import logo from "./logo.png"
const Navbar = ({ isLogin, setIsLogin, setLoginPopUp, setRegisterPopUp }) => {
	const showLogin = () => setLoginPopUp(true)
	const showRegister = () => setRegisterPopUp(true)

	return (
		<Box sx={{ borderBottom: `1px solid ${blue[100]}`, padding: '1rem' }}>
			<Container maxWidth="md">
				<Box display="flex" justifyContent="space-between" >
						

						<img src={logo} height={150} width={200} style={{position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, justifyContent: 'center',}}  />
						
			
					<Button 
						sx = {{fontSize: 20, color: '#9fa8da' }}
						component={NavLink} to="/">
						Informatics.GE
					</Button>
					<Box sx = {{alignItems: 'flex-end' ,display: "flex", justifyContent: "space-between" } }>
						
						<Button 
							sx={{ marginInline: '2px', alignSelf: 'flex-end' }}
							component={NavLink}
							to="/compiler"
						>
							კომპილატორი
						</Button>
						<Button
							sx={{ marginInline: '2px' }}
							component={NavLink}
							to="/contests"
						>
							კონტესტები
						</Button>
						<Button
							sx={{ marginInline: '2px'}}
							component={NavLink}
							to="/materials"
						>
							მასალები


						</Button>
						{isLogin ? (
							<Button
								sx={{ marginInline: '2px' , marginLeft: '60px'}}
								onClick={() => console.log('Wubba Lubba Dub Dub')}
							>
								გამოსვლა
							</Button>
						) : (
							<>
								<Button
									sx={{ marginInline: '2px' , alignSelf: 'right', marginLeft: '60px'}}
									variant="contained"
									onClick={showLogin}
								>
									შესვლა
								</Button>
								<Button
									sx={{ marginInline: '2px' , alignSelf: 'flex-end'}}
									variant="outlined"
									onClick={showRegister}
								>
									რეგისტრაცია
								</Button>
								
							</>
						)}
					</Box>
				</Box>
			</Container>
		</Box>
	)
}

export default Navbar

