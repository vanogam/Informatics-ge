import { Box, Button, Container, Typography } from '@mui/material'
import { blue } from '@mui/material/colors'
import { NavLink } from 'react-router-dom'
import axios from 'axios'
import { toast } from 'react-toastify'
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
			.then((response) => {
				toast.success('Logout Successful')
				authContext.logout()
			})
			.catch((error) => console.log(error))
	}

	return (
		<Box
			display="flex"
			alignItems="center"
			justifyContent="space-between"
			sx={{
				borderBottom: `1px solid ${blue[100]}`,
				padding: '1rem 4rem',
				background: '#3c324e',
			}}
		>
			<Button
				sx={{
					// marginLeft: '5%',
					fontSize: 20,
					fontWeight: 'bold',
					color: '#e1dce6',
				}}
				component={NavLink}
				to="/"
			>
				Informatics.GE
			</Button>
			{(toggleMenu || screenWidth > 500) && (
				<Box
					sx={{
						width: '100%',
						paddingLeft: '12rem',
						alignItems: 'center',
						display: 'flex',
						justifyContent: 'space-between',
						color: '#e1dce6',
					}}
				>
					<Box
						display="flex"
						sx={{
							// marginLeft: '10%',
							alignItems: 'center',
							display: 'flex',
							justifyContent: 'space-between',
						}}
					>
						<Button
							className="items"
							sx={{
								// marginLeft: '10%',
								// marginInline: '2px',
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
								color: '#e1dce6',
							}}
							component={NavLink}
							to="/contests"
						>
							კონტესტები
						</Button>
						<Button
							className="items"
							sx={{
								color: '#e1dce6',
							}}
							component={NavLink}
							to="/materials"
						>
							მასალები
						</Button>
					</Box>

					<Box
						display="flex"
						alignItems="flex-end"
						justifyContent="space-between"
					>
						{isLoggedIn ? (
							<div style={{ display: 'flex', alignItems: 'center' }}>
								<Typography variant='button'>{authContext.username}</Typography>
								<Button
									className="items"
									sx={{
										// marginInline: '2px',
										// marginLeft: '60px',
										color: '#e1dce6',
									}}
									onClick={() => handleLogOut()}
								>
									გამოსვლა
								</Button>
							</div>
						) : (
							<>
								<Login />
								<Register />
							</>
						)}
					</Box>
				</Box>
			)}
			<button onClick={toggleNav} className="btn">
				☰
			</button>
		</Box>
	)
}

export default Navbar
