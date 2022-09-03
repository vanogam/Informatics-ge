import { useParams } from 'react-router-dom'
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

function handleResults(response, setResults){
    console.log(response)
	var curResults =  []
	const standings = response.data.standings
	for(const standing of standings){
		const standingItem = {
			name: standing.username,
			score: standing.totalScore
		}
		curResults.push(standingItem)
	}
	console.log(curResults)
	setResults(curResults)
}
export default function Results(){
    const {contest_id} = useParams()
	const [results, setResults] = useState([])
	useEffect(() => {
		axios
			.get(`http://localhost:8080/contest/${contest_id}/standings?offset=0&limit=20`,
			{params: {
				offset : 0 , 
				limit: 20
			}})
			.then((response) =>  handleResults(response, setResults))
			.catch((error) => console.log(error))
	}, [])

    return (
       <main>
			<Typography  variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}>
				კონტესტის შედეგები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.4rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
			>
			</Typography>
			<Container maxWidth="lg">
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							
							<TableCell>მომხმარებელი 👨‍💻</TableCell>
                            <TableCell>შედეგი 🏆</TableCell>
						</TableRow>
					</TableHead>
					<TableBody>
						{results.map((result) => (
							<TableRow
							
								key={result.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
                                <TableCell component="th" scope="row">
									{result.name}

								</TableCell>
								<TableCell component="th" scope="row">
									{result.score}

								</TableCell>

							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)

};