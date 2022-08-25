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
export default function Contest(){
    const {contest_id} = useParams()
    const rows = [
		{
			id: 1,
            category: "a",
			name: 'Problem1',
			
		},
        {
			id: 2,
            category: "b",
			name: 'Problem2',
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
							
							<TableCell>კატეგორია</TableCell>
							<TableCell >სახელი</TableCell>
					
						</TableRow>
					</TableHead>
					<TableBody>
						{rows.map((row) => (
							<TableRow
							
								key={row.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
                                <TableCell component="th" scope="row">
									{row.category}

								</TableCell>
								<TableCell ><NavLink to ={`/problem/${row.id}`} exact>{row.name} </NavLink></TableCell>
							
							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
    
    

};