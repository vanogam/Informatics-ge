import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
} from '@mui/material'

export default function Contests() {
	const mockData = [
		{
			id: 1,
			name: 'Impel Hackathon',
			status: 'ongoing',
			startDate: '10.06.2022 15:45',
			duration: '01:30',
			contestants: 44
		},
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
						{/* {rows.map((row) => (
							<TableRow
								key={row.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								<TableCell component="th" scope="row">
									{row.name}
								</TableCell>
								<TableCell align="right">{row.calories}</TableCell>
								<TableCell align="right">{row.fat}</TableCell>
								<TableCell align="right">{row.carbs}</TableCell>
								<TableCell align="right">{row.protein}</TableCell>
							</TableRow>
						))} */}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
}
