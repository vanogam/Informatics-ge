import { useParams } from 'react-router-dom'
import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
	Button,
	Modal,
	Box,
	TableContainer,
	Paper
} from '@mui/material'
import { NavLink } from "react-router-dom";
import axios from 'axios';
import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../store/authentication'
import ContestRegisterPopUp from '../Components/ContestRegisterPopUp'
function handleContestResponse(response, setProblems){
	var curTasks = []
	const tasks = response.data.tasks
	var curCategory = 1
	for(const task of tasks){
		
		const taskId = task.task.id
		const taskName = task.task.title.KA
		const taskItem = {
			id: taskId, 
			name: taskName,
			category: curCategory
		}
		curCategory += 1
		curTasks.push(taskItem)
	}
	setProblems(curTasks)
}
export default function Contest(){
	const [popUp, setPopUp] = useState(false)
	const authContext = useContext(AuthContext)
	const isLoggedIn = authContext.isLoggedIn
	const [roles, setRoles] = useState()
    const {contest_id} = useParams()
	const [problems , setProblems] = useState([])
	const [registered, setIsRegistered] = useState(false)
	useEffect(() => {
		axios
			.get(`http://localhost:8080/contest/${contest_id}/tasks`, {
				params:{
					offset : 0 , 
					limit: 20
				}
			})
			.then((response) =>  {
				handleContestResponse(response, setProblems)
				axios
			.get(`http://localhost:8080/contest/${contest_id}/is-registered`)
			.then((response) => {
				
                if (response.data.registered){
                    setIsRegistered(true)
                }})
				})
			.catch((error) => console.log(error))
			setRoles(() => localStorage.getItem('roles'))
	}, [])

    // const rows = [
	// 	{
	// 		id: 1,
    //         category: "a",
	// 		name: 'Problem1',
			
	// 	},
    //     {
	// 		id: 2,
    //         category: "b",
	// 		name: 'Problem2',
	// 	},
    // ]
    return (
       <main>
		<Modal open={popUp} onClose={() => setPopUp(false)}>
				<Box
					sx={{
						position: 'absolute',
						top: '50%',
						left: '50%',
						transform: 'translate(-50%, -50%)',
						width: '350px',
						bgcolor: 'white',
						border: `2px solid ;`,
						borderRadius: '0.5rem',
						boxShadow: 24,
						p: 4,
					}}
				>
				<ContestRegisterPopUp contestId = {contest_id} />
				</Box>
				</Modal>
			<Typography variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}>
				კონტესტის ამოცანები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
			>
				
			</Typography>
			<Container maxWidth="lg">
			<TableContainer component={Paper} sx={{ marginInline: 'auto' }}>
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							
							<TableCell>კატეგორია</TableCell>
							<TableCell >სახელი</TableCell>
							{(isLoggedIn && roles !== 'ADMIN' && registered === false) && (
									<TableCell>
										<Button
											className="items"
											variant="contained"	
											color = "success"
											sx={{ 	background: '#3c324e' }}
											onClick = {() => {setPopUp(true)}}
			
										>
											რეგისტრაცია
										</Button>
									</TableCell>
								)}
						</TableRow>
					</TableHead>
					<TableBody>
						{problems.map((problem) => (
							<TableRow
							
								key={problem.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
                                <TableCell component="th" scope="row">
									{problem.category}

								</TableCell>
								<TableCell ><NavLink style={{ color: 'black', textDecorationLine: 'none' }} to={`${problem.id}`}exact>{problem.name} </NavLink></TableCell>

							</TableRow>
						))}
					</TableBody>
				</Table>
				</TableContainer>
			</Container>
		</main>
	)
    
};