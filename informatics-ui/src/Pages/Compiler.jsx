import * as React from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import {  Button } from '@mui/material'

const boxStyle = {
	display: 'flex',
	flexDirection: 'column',
	 '& .MuiTextField-root': { m: 1, width: '25ch' },
	 paddingLeft: "30%",
	 marginLeft: "10%",
	 paddingTop: "5%",
	 color: 'purple',
	 paddingLeft: '10%',
	 fontWeight: 'bold',

	 fontSize: '20',

}
const Compiler = () => {

  
	return ( 
		<Box sx = {{
			
			 display: 'flex',
		flexDirection: 'row'}}>

	
		 <Box
		component="form"
	
		sx={{
			paddingTop: '50px',
		'& .MuiTextField-root': { m: 1, width: '50ch' },
		color: 'purple',

		paddingLeft: '10%',
		fontWeight: 'bold',
		display: "flex",
		flexDirection: 'column',

		fontSize: '20',

		}}
		>	

		<p >შეიყვანე კოდი: </p>
			<TextField
			
			id="outlined-multiline-static"
			multiline
			rows={20}
			>

			</TextField>

			<Button className = "items"
									sx={{ marginInline: '2px' , alignSelf: 'right', marginLeft: '60px' ,   width : "50%",
									background: 'rgb(42,13,56)',
									background: 'linear-gradient(90deg, rgba(42,13,56,1) 63%, rgba(53,26,88,1) 77%, rgba(73,62,153,1) 92%)'}}
									variant="contained"
								
								>
									დაკომპილირება
								</Button>
		</Box>
		
		
		<Box
		sx = {boxStyle}
		component="form"
		>
			<p >შემავალი მონაცემები: </p>
			<TextField
		
			id="outlined-multiline-static"
			multiline
			rows={5}
			>

	
			</TextField>

			<Button className = "items"
									sx={{ marginInline: '2px' , alignSelf: 'right',    width : "50%", marginBottom: "20%", marginLeft: '30px' ,
									background: 'rgb(42,13,56)',
									background: 'linear-gradient(90deg, rgba(42,13,56,1) 63%, rgba(53,26,88,1) 77%, rgba(73,62,153,1) 92%)'}}
									variant="contained"
								
								>
									ფაილის ატვირთვა
								</Button>
			
			<p >გამომავალი მონაცემები: </p>
			<TextField
			id="outlined-multiline-static"
			multiline
			rows={4}
			>

			</TextField>
			</Box>
		
		</Box>
			
	
	)
}

export default Compiler
