import dayjs, { Dayjs } from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import {
	Button,
	Container,
	MenuItem,
	Paper,
	Stack,
	TextField,
	Typography,
} from '@mui/material'

import { useRef, useState } from 'react'
import axios from 'axios'
import { NavLink } from 'react-router-dom'
export default function NewNews(){

	const title = useRef(null)
	const time = useRef(null)
    const text = useRef(null)
	const picRef = useRef(null)



	const handleAddNews = (params) => {
		
	}
  return(  
    <LocalizationProvider dateAdapter={AdapterDayjs}>
			<Container maxWidth="xs">
				<Stack gap="1rem" marginTop="2rem">

					<Paper elevation={4} sx={{ padding: '1rem' }}></Paper>
    <Paper elevation={4} sx={{  padding: '1rem' }}>
    <Typography align="center" variant="h6" mb="1rem">
       სიახლე
    </Typography>
    
    <Stack gap="1rem" maxWidth="25rem" mx="auto" mb="1rem">
        <Stack flexDirection="row" gap="1rem">
            
        </Stack>
        <Stack flexDirection="row" gap="1rem">
        <Button variant="contained" component="label">
                სურათი
                <input ref={picRef} type="file" hidden />
            </Button>
            <TextField
                label="სათაური"
                inputRef={title}
                variant="outlined"
                size = "small"
            />
             <TextField
                label="დრო"
                inputRef={time}
                variant="outlined"
                size = "small"
            />
            </Stack>
            <TextField
                label="ტექსტი"
                inputRef={text}
                variant="outlined"
                multiline
                rows={10}
            
            />
            <Button 
            
            sx ={{ marginLeft: '25%',width:'50%'}} variant="contained" component="label"
            onClick={() => {(console.log("Save news"))}}>
                შენახვა
                
            </Button>
            <Button
							sx = {{background: '#3c324e'}}variant="contained" size="large"
							component={NavLink}
							to="/"
						>
							დასრულება
                            </Button>
  
        {/* <TextField type="file" variant="outlined" /> */}

     
       
    
    </Stack>
    </Paper>
</Stack>
</Container>
</LocalizationProvider>
)}
