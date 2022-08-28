import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
} from '@mui/material'

export default function Materials() {
	const rows = [
		{
			id: 1,
			name: 'Binary Search',
		},
		{
			id: 1,
			name: 'Graphs',
		},
	]
	
	return (
		<main>
			<Typography variant="h6" fontWeight="bold" mt="1rem" align="center"
			sx = {{color:'#452c54', fontWeight: 'bold'}}>
				მასალები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
				sx = {{color: '#281d2e'}}
			>
				აქ შეგიძლიათ გაეცნოთ სხვადასხვა მასალებს 
			</Typography>
			<Container maxWidth="lg">
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							<TableCell>თემატიკა</TableCell>
							<TableCell ></TableCell>
						
						</TableRow>
					</TableHead>
					<TableBody>
						{rows.map((row) => (
							<TableRow
								
								key={row.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
								<TableCell component="th" scope="row">
									{row.name}
								</TableCell>
							
							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
}
