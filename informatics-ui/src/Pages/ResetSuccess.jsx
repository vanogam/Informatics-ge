
import * as React from 'react';
import Box from '@mui/material/Box';
import InputLabel from '@mui/material/InputLabel';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import logo from '../Components/logo.png'
import EmailIcon from '@mui/icons-material/Email';
import { Button } from '@mui/material'
import { useState } from 'react';
import Typography from '@mui/material/Typography';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import LockIcon from '@mui/icons-material/Lock';
export default function ResetSuccess() {
	const [newPassowrd, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
	const [success, setSuccess] = useState('');
	const handleInputChange = (e) => {
        const {id , value} = e.target;
        if (id === "new-password"){
            setNewPassword(value)
        }
        if (id === "confirm-password"){
            setConfirmPassword(value)
        }
	
	}

	const handleReset = () => {
		if (newPassowrd === confirmPassword){
            setSuccess("True")
        }else{
            setSuccess("False")
        }
		
		// const body = {'username': registerUsername, 'firstName': registerFirstName, 'lastName': registerLastName, 'password': registerPassword}
		// console.log(registerEmail, registerFirstName, registerLastName, registerPassword, registerUsername)
		// axios.post('http://localhost:8080/register', body).then(response => console.log(response));
	}
  return (
    <Box sx ={{display:'flex', flexDirection: 'row' ,marginLeft : '25%'}}>
        <Box>
			 <Box sx={{ '& > :not(style)': { marginLeft: '25%', marginTop: '15%'} }}>
		   <InputLabel >
          შეიყვანე ახალი პაროლი
        </InputLabel>
		 {/* <Typography gutterBottom variant="p" component="div">
          შეიყვანე ელ-ფოსტა: </Typography> */}
        <Box>
        <TextField
        id="new-password"
				label="პაროლი"
				type="password"
				autoComplete="current-password"
				value={newPassowrd} 
				onChange = {(e) => handleInputChange(e)} 
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<LockIcon></LockIcon>
					  </InputAdornment>
					)}}
				/>

        </Box>
        <Box>
        <TextField
        id="confirm-password"
				label="გაიმეორე პაროლი"
				type="password"
				autoComplete="current-password"
				value={confirmPassword} 
				onChange = {(e) => handleInputChange(e)} 
				InputProps={{
					startAdornment: (
					  <InputAdornment position="start">
						<LockIcon></LockIcon>
					  </InputAdornment>
					)}}
				/>
       
        </Box>
      
	  
    </Box>
	

	<Button sx = {{marginLeft: '30%', marginTop: '12%',	background: 'rgb(42,13,56)'}}onClick={()=>handleReset()} variant="contained">
პაროლის აღდგენა
</Button>
    {success==="True" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '30%', marginTop:'3%'}}><CheckCircleIcon></CheckCircleIcon>
					<Typography gutterBottom variant="p" component="div" sx = {{color: 'green'}}>
          პაროლი შეიცვალა </Typography> </Box>
			
					)}
	{success==="False" && (<Box  display="flex" justifyContent="row" sx ={{marginLeft: '30%', marginTop:'3%'}}><CancelIcon></CancelIcon>
				<Typography gutterBottom variant="p" component="div" sx = {{color: 'red'}}>
		პაროლები არ ემთხვევა </Typography> </Box>
		
				)}
	</Box>
    
    <Box sx = {{marginLeft: '5%', marginTop: '10%'}}> 
    <img src={logo} height={'90%'} width={'50%'} />
    </Box>
    </Box>
    
	
  );
}
