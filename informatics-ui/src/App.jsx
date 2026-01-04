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
import NewContest from './Pages/contest/NewContest'
import EditContest from './Pages/contest/EditContest'
import Results from './Pages/Results'
import MySubmissions from './Pages/MySubmissions'
import ContestSubmissions from './Pages/ContestSubmissions'
import NewTaskCard from './Components/newtask/NewTaskCard'
import Error from './Pages/Error'
import { useEffect } from 'react'
import axios from 'axios'
import Cookies from "js-cookie";
import { renewCsrfToken } from './utils/csrfUtils'
import AddPost from "./Pages/AddPost";
import AdminPanel from "./Pages/AdminPanel";
import { ConfirmDialogProvider } from './utils/ConfirmDialogContext'

function App() {
	useEffect(() => {
		if (Cookies.get('XSRF-TOKEN') === undefined) {
			renewCsrfToken();
		}
	}, []);

	return (
		<ConfirmDialogProvider>
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
						path='/contest/:contest_id/problem/:problem_id'
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
					<Route path='/contest/:contest_id/edit' element={<EditContest />} />\
					<Route path='/contest/:contest_id/task/add' element={<NewTaskCard />} />\
					<Route path='/task/:taskId' element={<NewTaskCard />} />\
					<Route path='/room/:room_id/post' element={<AddPost />} />
					<Route path='/room/:room_id/post/:post_id' element={<AddPost />} />
					<Route path='/admin' element={<AdminPanel />} />
					<Route element={<Error/>} />
				</Routes>
			</Box>
		</div>
		</ConfirmDialogProvider>
	)
}

export default App
// #mvn clean install
// #sudo docker-compose up --build