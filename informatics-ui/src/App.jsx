import { Box } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import Navbar from './Components/NavBar'
import Compiler from './Pages/Compiler'
import Contests from './Pages/Contests'
import Main from './Pages/Main'
import ResetSuccess from './Pages/ResetSuccess'
import ResetPassword from './Pages/ResetPassword'
import Materials from './Pages/Materials'
import Contest from './Pages/Contest'
import Problem from './Pages/Problem'
import NewContest from './Pages/NewContest'
function App() {
	return (
		<div className="App">
			<Box display="flex" flexDirection="column">
				<Navbar />

				<Routes>
					<Route path="/" element={<Main />} />
					<Route path="/compiler" element={<Compiler />} />
					<Route path="/reset" element={<ResetPassword />} />
					{/* Reset Password Sample */}
					{/* /recover/update-password/RrjT9ez3cQNoctAkGZp0+8wTcSYfAducOnahwj2Y */}
					<Route path="recover">
						<Route path="update-password">
							<Route path=":token" element={<ResetSuccess />} />
						</Route>
					</Route>
					<Route path="/resetSuccess" element={<ResetSuccess />} />
					<Route path="/materials" element={<Materials />} />
					<Route path="/contests" element={<Contests />} />
					<Route path="/contest">
						<Route path=":contest_id" element={<Contest />} />
					</Route>
					<Route
						path="/contest/:contest_id/:problem_id"
						element={<Problem />}
					/>
					<Route path="/addContest" element={<NewContest />} />
				</Routes>
			</Box>
		</div>
	)
}

export default App
// #mvn clean install
// #sudo docker-compose up --build