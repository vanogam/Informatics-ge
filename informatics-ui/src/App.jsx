import { useState } from 'react'
import { Box } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import Navbar from './Components/NavBar'
import Compiler from './Pages/Compiler'
import Contests from './Pages/Contests'
import Main from './Pages/Main'
import ResetSuccess from './Pages/ResetSuccess'
import ResetPassword from './Pages/ResetPassword'
import Materials from './Pages/Materials'

function App() {

	return (
		<div className="App">
			<Box display="flex" flexDirection="column">
				<Navbar />

				<Routes>
					<Route path="/" element={<Main />} />
					<Route path="/compiler" element={<Compiler />} />
					<Route path="/reset" element={<ResetPassword />} />
					<Route path="/resetSuccess" element={<ResetSuccess />} />
					<Route path="/materials" element={<Materials />} />
					<Route path="/contests" element={<Contests />} />
				</Routes>
			</Box>
		</div>
	)
}

export default App
// #mvn clean install
// #sudo docker-compose up --build
