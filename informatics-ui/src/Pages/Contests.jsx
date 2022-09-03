import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
	Button,
} from '@mui/material'
import { NavLink } from 'react-router-dom'
import axios from 'axios'
import { useState, useEffect, useContext } from 'react'
import { AuthContext } from '../store/authentication'

function handleContestsResponse(response, setRows, isLoggedIn) {
	var curRows = []
	const contests = response.data.contests
	for (const contest of contests) {
		const contestId = contest.id
		const contestName = contest.name
		const contestStartDate = contest.startDate
		const contestDuration = (contest.durationInSeconds / 3600).toFixed(2)
		const contestStatus = contest.status
		var curContest = {
			id: contestId,
			name: contestName,
			status: contestStatus,
			startDate: contestStartDate,
			duration: contestDuration.toString() + ' სთ',
			status: contestStatus,
			results: 'results',
		}
		curRows.push(curContest)
	}
	setRows(curRows)
}

export default function Contests() {
	const authContext = useContext(AuthContext)
	const isLoggedIn = authContext.isLoggedIn
	const [rows, setRows] = useState([])
	const [roles, setRoles] = useState()
	useEffect(() => {
		axios
			.get('http://localhost:8080/contest-list', {
				params: {
					roomId: 1,
				},
			})
			.then((response) => handleContestsResponse(response, setRows, isLoggedIn))
			.catch((error) => console.log(error))
		setRoles(() => localStorage.getItem('roles'))
	}, [])

	return (
		<main>
			<Typography
				variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}
			>
				კონტესტები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
				sx={{ color: '#281d2e' }}
			>
				ამ გვერდზე შეგიძლიათ იხილოთ ჩვენი კონტესტები და მიიღოთ მათში
				მონაწილეობა.
			</Typography>
			<Container maxWidth="lg">
				{roles === 'ADMIN' && (
					<Button
						variant="contained"
						color="secondary"
						sx={{ backgroundColor: '#2f2d47' }}
						component={NavLink}
						to="/addContest"
					>
						დაამატე კონტესტი
					</Button>
				)}
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							<TableCell>სახელი</TableCell>
							<TableCell align="right">დასაწყისი</TableCell>
							<TableCell align="right">ხანგრძლივობა</TableCell>
							<TableCell align="right">სტატუსი</TableCell>
							<TableCell align="right">შედეგები</TableCell>
							{roles === 'ADMIN' ? <TableCell></TableCell> : null}
						</TableRow>
					</TableHead>
					<TableBody>
						{rows.map((row) => (
							<TableRow
								key={row.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								<TableCell component="th" scope="row">
									<NavLink to={`/contest/${row.id}`} exact>
										{row.name}{' '}
									</NavLink>
								</TableCell>
								<TableCell align="right">{row.startDate}</TableCell>
								<TableCell align="right">{row.duration}</TableCell>
								<TableCell align="right">{row.status}</TableCell>
								<TableCell align="right">{row.results}</TableCell>
								{roles === 'ADMIN' && (
									<TableCell>
										<Button
											variant="contained"
											color="secondary"
											sx={{ backgroundColor: '#2f2d47' }}
											component={NavLink}
											to={`/editContest/${row.id}`}
										>
											რედაქტირება
										</Button>
									</TableCell>
								)}
								{(roles !== 'ADMIN') && (
									<TableCell>
										<Button
											variant="contained"
											color="secondary"
											sx={{ backgroundColor: '#2f2d47' }}
											onClick = {() => axios.post(`http://localhost:8080/contest/${row.id}/register`, {})}
										
										>
											რეგისტრაცია
										</Button>
									</TableCell>
								)}

							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
}
