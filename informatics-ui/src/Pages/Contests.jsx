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
export default function Contests() {
	const rows = [
		{
			id: 1,
			name: 'FreeUni Hackaton1',
			status: 'ongoing',
			startDate: '10.06.2022 15:45',
			duration: '01:30',
			contestants: 44,
			results: 50
		},
		{
			id: 2,
			name: 'FreeUni Hackaton2',
			status: 'ongoing',
			startDate: '10.06.2022 15:45',
			duration: '01:30',
			contestants: 44,
			results: 50
		},
		{
			id: 3,
			name: 'FreeUni Hackaton3',
			status: 'ongoing',
			startDate: '10.06.2022 15:45',
			duration: '01:30',
			contestants: 44,
			results: 50
		},
		{
			id: 4,
			name: 'FreeUni Hackaton4',
			status: 'ongoing',
			startDate: '10.06.2022 15:45',
			duration: '01:30',
			contestants: 44,
			results: 50
		}
	]
	
	return (
		<main>
			<Typography variant="h5" fontWeight="bold" mt="1rem" align="center">
				კონტესტები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
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
							<TableCell align="right">მონაწილეთა რაოდენობა</TableCell>
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
								<TableCell align="right">{row.contestants}</TableCell>
								<TableCell align="right">{row.results}</TableCell>
							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
}
