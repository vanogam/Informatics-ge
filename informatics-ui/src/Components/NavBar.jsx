import {  Box, Button, Container, Link } from '@mui/material'
import { blue, indigo } from '@mui/material/colors'
import { NavLink } from 'react-router-dom'
import logo from "./logo.png"
import axios from 'axios';
import './NavBar.css'


import React, {useState, useEffect} from 'react'
const changeAuthStatus = (response, setIsLogin) => {
	if (response.status === 200){
		setIsLogin(false)
	}
		
}
const handleLogOut = (setIsLogin) => {
	// setEmail(email)
	// setPassword(password)
	console.log("logging out")
	setIsLogin(false)
	const body = {}
	axios.post('http://localhost:8080/logout', body )
		.then(response => changeAuthStatus(response, setIsLogin));
}

const Navbar = ({ isLogin, setIsLogin, setLoginPopUp, setRegisterPopUp }) => {
	const showLogin = () => setLoginPopUp(true)
	const showRegister = () => setRegisterPopUp(true)
	const [toggleMenu, setToggleMenu] = useState(false)
 	const [screenWidth, setScreenWidth] = useState(window.innerWidth)
	const toggleNav = () => {
	setToggleMenu(!toggleMenu)
	}

	useEffect(() => {

	const changeWidth = () => {
		setScreenWidth(window.innerWidth);
	}

	window.addEventListener('resize', changeWidth)

	return () => {
		window.removeEventListener('resize', changeWidth)
	}

	  }, [])
	return (
		<Box sx={{ borderBottom: `1px solid ${blue[100]}`, padding: '1rem' ,  
		background: 'rgb(42,13,56)',
		background:' linear-gradient(90deg, rgba(42,13,56,1) 32%, rgba(8,14,130,0.5214679621848739) 100%, rgba(0,212,255,1) 100%)'}}>
			<Container maxWidth="md">
				<Box display="flex" justifyContent="space-between" >
			
					<Button 
						sx = {{flex: '50%',fontSize: 20, color: '#e1dce6', alignSelf: "left"}}
					
						component={NavLink} to="/">
						Informatics.GE
					</Button>
					{(toggleMenu || screenWidth > 500) && (
					<Box className = "list"
					 sx = {{alignItems: 'flex-end' ,display: "flex", justifyContent: "space-between", color: '#e1dce6' } }>
						
						<Button className = "items"
							sx={{ marginInline: '2px', alignSelf: 'flex-end' , color: '#e1dce6'}}
							component={NavLink}
							to="/compiler"
						>
							კომპილატორი
						</Button>
						<Button className = "items"
							sx={{ marginInline: '2px', color: '#e1dce6' }}
							component={NavLink}
							to="/contests"
						>
							კონტესტები
						</Button>
						<Button className = "items"
							sx={{ marginInline: '2px',color: '#e1dce6'}}
							component={NavLink}
							to="/materials"
						>
							მასალები


						</Button>
						{isLogin ? (
							<Button className = "items"
								sx={{ marginInline: '2px' , marginLeft: '60px',color: '#e1dce6'}}
								onClick={()=>handleLogOut(setIsLogin)}
							>
								გამოსვლა
							</Button>
						) : (
							<>
								<Button className = "items"
									sx={{ marginInline: '2px' , alignSelf: 'right', marginLeft: '60px' ,  
									background: 'rgb(42,13,56)',
									background: 'linear-gradient(90deg, rgba(42,13,56,1) 63%, rgba(53,26,88,1) 77%, rgba(73,62,153,1) 92%)'}}
									variant="contained"
									onClick={showLogin}
								>
									შესვლა
								</Button>
								<Button  className = "items"
									sx={{ marginInline: '2px' , alignSelf: 'flex-end', color: '#e1dce6'}}
									// background: 'linear-gradient(90deg, rgba(141,114,154,1) 63%, rgba(115,81,159,1) 77%, rgba(33,30,52,1) 92%)'}}
									// variant="outlined"
									onClick={showRegister}
								>
									რეგისტრაცია
								</Button>
								
							</>
						)}
					</Box> )}
					<button sx ={{backgroundColor:"purple"}} onClick={toggleNav} className="btn">☰</button>
				</Box>
			</Container>
		</Box>
	)
}

export default Navbar

