
import * as React from 'react';
import Box from '@mui/material/Box';
import InputLabel from '@mui/material/InputLabel';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';

import EmailIcon from '@mui/icons-material/Email';
import { Button } from '@mui/material'
import { useState } from 'react';
import Typography from '@mui/material/Typography';
import logo from '../Components/logo.png'
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
export default function ResetPassword() {
	const [email, setEmail] = useState('');
	const [success, setSuccess] = useState('');
	const handleInputChange = (e) => {
		setEmail(e.target.value)
	
	}

	const handleReset = () => {
		console.log(email)
		setSuccess("True")
		// const body = {'username': registerUsername, 'firstName': registerFirstName, 'lastName': registerLastName, 'password': registerPassword}
		// console.log(registerEmail, registerFirstName, registerLastName, registerPassword, registerUsername)
		// axios.post('http://localhost:8080/register', body).then(response => console.log(response));
	}
  return (
	<Box sx ={{display:'flex', flexDirection: 'row' ,marginLeft : '25%', marginTop: '2%'}}>
		<Box>
			 <Box sx={{ '& > :not(style)': { marginLeft: '25%', marginTop: '15%'} }}>
		   <InputLabel >
          შეიყვანეთ ელ-ფოსტა:
        </InputLabel>
		 {/* <Typography gutterBottom variant="p" component="div">
          შეიყვანე ელ-ფოსტა: </Typography> */}
     <TextField
	 	
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<EmailIcon></EmailIcon>
					  </InputAdornment>
					)}}
					id="register-email"
					type="email"
					autoComplete="current-email"
					value={email}
					onChange = {(e) => handleInputChange(e)} 
					
				/>
	  
    </Box>

	<Button sx = {{marginLeft: '30%', marginTop: '12%',	background: 'rgb(42,13,56)'}}onClick={()=>handleReset()} variant="contained">
პაროლის აღდგენა
</Button>
    {success==="True" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '30%', marginTop:'12%'}}><CheckCircleIcon></CheckCircleIcon>
					<Typography gutterBottom variant="p" component="div" sx = {{color: 'green',    fontSize: '0.8rem',}}>
        ლინკი გადმოგზავნილია ელ-ფოსტაზე </Typography> </Box>
			
					)}
	{success==="False" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '30%', marginTop:'12%'}}><CancelIcon></CancelIcon>
				<Typography gutterBottom variant="p" component="div" sx = {{color: 'red', fontSize: '0.8rem'}}>
		მომხმარებელი არ მოიძებნა </Typography> </Box>
		
				)}
	</Box>

	<Box sx = {{marginLeft: '5%', marginTop: '7%'}}> 
    <img src={logo} height={'90%'} width={'50%'} />
    </Box>
	
	</Box>
	
   
  );
}
