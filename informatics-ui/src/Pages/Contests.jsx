import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
} from '@mui/material'
import { NavLink } from "react-router-dom";
import axios from 'axios';
import { useState, useEffect } from 'react';

function handleContestsResponse(response, setRows){
	var curRows = []
	const contests = response.data.contests 
	for(const contest of contests){
		const contestId = contest.id
		const contestName = contest.name
		const contestStartDate = contest.startDate
		const contestDuration = (contest.durationInSeconds/3600).toFixed(2)
		const contestStatus = contest.status 
		const curContest = {
			id: contestId,
			name : contestName,
			status: contestStatus,
			startDate: contestStartDate, 
			duration: contestDuration.toString() + ' სთ',
			status: contestStatus,
			results: "results"
		}
		curRows.push(curContest)
		
	}
	setRows(curRows)
}

export default function Contests() {
	const [rows, setRows] = useState([])
	
	useEffect(() => {
		axios
			.get('http://localhost:8080/contest-list', {
				params:{
					"roomId":1
				}
			})
			.then((response) =>  handleContestsResponse(response, setRows))
			.catch((error) => console.log(error))
	}, [])
	return (
		<main>
			<Typography variant="h6" fontWeight="bold" mt="1rem" align="center"
				sx = {{color:'#452c54', fontWeight: 'bold'}}>
				კონტესტები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
				sx = {{color: '#281d2e'
					}}
			
			>
				ამ გვერდზე შეგიძლიათ იხილოთ ჩვენი კონტესტები და მიიღოთ მათში
				მონაწილეობა.
			</Typography>
			<Container maxWidth="lg">
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							<TableCell>სახელი</TableCell>
							<TableCell align="right">დასაწყისი</TableCell>
							<TableCell align="right">ხანგრძლივობა</TableCell>
							<TableCell align="right">სტატუსი</TableCell>
							<TableCell align="right">შედეგები</TableCell>
						</TableRow>
					</TableHead>
					<TableBody>
						{rows.map((row) => (
							<TableRow
							
								key={row.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
								<TableCell component="th" scope="row">
									
									<NavLink to ={`/contest/${row.id}`} exact>{row.name} </NavLink>
								</TableCell>
								<TableCell align="right">{row.startDate}</TableCell>
								<TableCell align="right">{row.duration}</TableCell>
								<TableCell align="right">{row.status}</TableCell>
								<TableCell align="right">{row.results}</TableCell>
							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
}
