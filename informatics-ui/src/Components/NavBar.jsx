import { Box, Button, Container, Link } from '@mui/material'
import { blue } from '@mui/material/colors'
import { NavLink } from 'react-router-dom'
import axios from 'axios'
import './NavBar.css'

import React, { useState, useEffect, useContext } from 'react'
import Login from './Login'
import Register from './Register'
import { AuthContext } from '../store/authentication'

const Navbar = () => {
	const authContext = useContext(AuthContext)
	const isLoggedIn = authContext.isLoggedIn
	const [toggleMenu, setToggleMenu] = useState(false)
	const [screenWidth, setScreenWidth] = useState(window.innerWidth)
	const toggleNav = () => {
		setToggleMenu(!toggleMenu)
	}

	useEffect(() => {
		const changeWidth = () => {
			setScreenWidth(window.innerWidth)
		}
		window.addEventListener('resize', changeWidth)
		return () => {
			window.removeEventListener('resize', changeWidth)
		}
	}, [])

	const handleLogOut = () => {
		axios
			.post('http://localhost:8080/logout', {})
			.then((response) => authContext.logout())
	}

	return (
		<Box
			display="flex"
			displayFlex="row"
			sx={{
				borderBottom: `1px solid ${blue[100]}`,
				padding: '1rem',
				background: '#3c324e',
			}}
		>
			<Button
				sx={{
					marginLeft: '5%',
					fontSize: 20,
					fontWeight: 'bold',
					color: '#e1dce6',
				}}
				component={NavLink}
				to="/"
			>
				Informatics.GE
			</Button>
			<Container maxWidth="md">
				<Box display="flex" justifyContent="space-between">
					{(toggleMenu || screenWidth > 500) && (
						<Box
							sx={{
								alignItems: 'flex-end',
								display: 'flex',
								justifyContent: 'space-between',
								color: '#e1dce6',
							}}
						>
							<Box></Box>
							<Box
								display="flex"
								flexDirection="row"
								sx={{
									marginLeft: '10%',
									alignItems: 'flex-end',
									display: 'flex',
									justifyContent: 'space-between',
								}}
							>
								<Button
									className="items"
									sx={{
										marginLeft: '10%',
										marginInline: '2px',
										alignSelf: 'flex-end',
										color: '#e1dce6',
									}}
									component={NavLink}
									to="/compiler"
								>
									კომპილატორი
								</Button>
								<Button
									className="items"
									sx={{
										marginLeft: '10%',
										marginInline: '2px',
										color: '#e1dce6',
									}}
									component={NavLink}
									to="/contests"
								>
									კონტესტები
								</Button>
								<Button
									className="items"
									sx={{ marginInline: '2px', color: '#e1dce6' }}
									component={NavLink}
									to="/materials"
								>
									მასალები
								</Button>
							</Box>

							<Box
								display="flex"
								flexDirection="row"
								sx={{
									marginLeft: '70%',
									alignItems: 'flex-end',
									display: 'flex',
									justifyContent: 'space-between',
								}}
							>
								{isLoggedIn ? (
									<Button
										className="items"
										sx={{
											marginInline: '2px',
											marginLeft: '60px',
											color: '#e1dce6',
										}}
										onClick={() => handleLogOut()}
									>
										გამოსვლა
									</Button>
								) : (
									<>
										<Login />
										<Register />
									</>
								)}
							</Box>
						</Box>
					)}
					<button
						sx={{ backgroundColor: 'purple' }}
						onClick={toggleNav}
						className="btn"
					>
						☰
					</button>
				</Box>
			</Container>
		</Box>
	)
}

export default Navbar
