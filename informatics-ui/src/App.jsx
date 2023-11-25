import { Box } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import Navbar from './Components/NavBar'
import Compiler from './Pages/Compiler'
import Contests from './Pages/Contests'
import Main from './Pages/Main'
import ResetSuccess from './Pages/ResetSuccess'
import ResetPassword from './Pages/ResetPassword'
import Archive from './Pages/Archive'
import Contest from './Pages/Contest'
import Problem from './Pages/Problem'
import NewContest from './Pages/NewContest'
import EditContest from './Pages/EditContest'
import Results from './Pages/Results'
import MySubmissions from './Pages/MySubmissions'
import ContestSubmissions from './Pages/ContestSubmissions'
import NewNews from './Pages/NewNews'

function App() {
	return (
		<div className='App'>
			<Box display='flex' flexDirection='column'>
				<Navbar />

				<Routes>
					<Route path='/' element={<Main />} />
					<Route path='/compiler' element={<Compiler />} />
					<Route path='/reset' element={<ResetPassword />} />
					{/* Reset Password Sample */}
					{/* /recover/update-password/RrjT9ez3cQNoctAkGZp0+8wTcSYfAducOnahwj2Y */}
					<Route path='recover'>
						<Route path='update-password'>
							<Route path=':token' element={<ResetSuccess />} />
						</Route>
					</Route>
					<Route path='/resetSuccess' element={<ResetSuccess />} />
					<Route path='/archive' element={<Archive />} />
					<Route path='/contests' element={<Contests />} />
					<Route path='/contest'>
						<Route path=':contest_id' element={<Contest />} />
					</Route>

					<Route path='/results'>
						<Route path=':contest_id' element={<Results />} />
					</Route>

					<Route
						path='/contest/:contest_id/:problem_id'
						element={<Problem />}
					/>
					<Route
						path='/contest/:contest_id/mySubmissions'
						element={<MySubmissions />}
					/>
					<Route
						path='/contest/:contest_id/submissions'
						element={<ContestSubmissions />}
					/>
					<Route path='/addContest' element={<NewContest />} />
					<Route path='/editContest/:contest_id' element={<EditContest />} />\
					<Route path='/newNews' element={<NewNews />} />
				</Routes>
			</Box>
		</div>
	)
}

export default App
// #mvn clean install
// #sudo docker-compose up --build