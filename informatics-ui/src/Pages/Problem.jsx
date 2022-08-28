import { useParams } from 'react-router-dom'
import { Document, Page ,pdfjs } from 'react-pdf';
import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
    Box
} from '@mui/material'
import React, { useState } from 'react'
import Editor from 'react-simple-code-editor'
import { highlight, languages } from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-javascript'
import 'prismjs/themes/prism.css' //Example style, you can use another
import './numbers.css'
import { Button } from '@mui/material'
import TextField from '@mui/material/TextField'
import { NavLink } from "react-router-dom";
import axios from 'axios';
import { useEffect } from 'react';
// import Pdf from "../../home/u/informatics/statements/passw/statement_KA.pdf"
pdfjs.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.js`;

const boxStyle = {
	display: 'flex',
	flexDirection: 'column',
	'& .MuiTextField-root': { m: 1, width: '25ch' },
	paddingLeft: '30%',
	marginLeft: '10%',
	paddingTop: '5%',
	color: 'purple',
	paddingLeft: '10%',
	fontWeight: 'bold',

	fontSize: '20',
}
function submitProblem(){
	// const body = {
	// 		"contestId" : 218,
	// 		"taskId" : 219,
	// 		"submissionText" : {{submissionText}},
	// 		"language" : "CPP"
	// }
	// axios
	// 		.post('http://localhost:8080/submit', {
	// 		})
	// 		.then((response) => handleLoginResponse(response))
	// 		.catch((error) => console.log(error))
}
const hightlightWithLineNumbers = (input, grammar, language) =>
	highlight(input, grammar, language)
		.split('\n')
		.map((line, i) => `<span class='editorLineNumber'>${i + 1}</span>${line}`)
		.join('\n')

function handleProblemResponse(response, setPDF){
	console.log(response)
	setPDF(response.data)
}

export default function Problem(){
    const {problem_id} = useParams()
    const [code, setCode] = React.useState(
		`#include <iostream>\nusing namespace std;\nint main()\n{\ncout << "Hello, World!";\nreturn 0; \n}\n`
	)
	const [pdf, setPDF] = useState("")
	// useEffect(() => {
	// 	axios
	// 		.get(`http://localhost:8080/statements/219/KA`)
	// 		.then((response) =>  {handleProblemResponse(response, setPDF)})
	// 		.catch((error) => console.log(error))
	// }, [])
	
	useEffect(() => {
		
    
          fetch(new Request(`http://localhost:8080/statements/${problem_id}/KA`,
		  {
			method: "GET",
			mode: "cors",
			cache: "default",
		  }
		))
            .then((response) => response.blob())
            .then((blob) => {              
              const file = window.URL.createObjectURL(blob);
             setPDF(file)
            })
           
	}, [])
    return (
        <Box sx={{
            display: 'flex',
            flexDirection: 'row',
            marginLeft: '10%'
        }}>
              <Box sx={{ marginLeft:'2%', marginTop: '5%',width: '80%', maxWidth: 500 }}>
        <Typography variant="h7" gutterBottom>
        ამოცანა {problem_id}
        </Typography>
        <Document file={pdf}>
            <Page pageNumber={1} />
        </Document>
        
       
      </Box>

      <Box
				sx={{
					paddingTop: '50px',
					'& .MuiTextField-root': { m: 1, width: '50ch' },

					paddingLeft: '10%',
					display: 'flex',
					flexDirection: 'column',

					fontSize: '20',
				}}
			>
				<p sx={{ color: 'purple' }}>შეიყვანე კოდი: </p>
				<Editor
					value={code}
					onValueChange={(code) => setCode(code)}
					highlight={(code) =>
						hightlightWithLineNumbers(code, languages.cpp, 'cpp')
					}
					className="editor"
					textareaId="codeArea"
					style={{
						overflowY: 'auto',
						height: '50vh',
						fontFamily: '"Fira code", "Fira Mono", monospace',
						fontSize: 12,
					}}
				/>
				<Button
					sx={{
						marginInline: '2px',
						alignSelf: 'right',
						marginLeft: '60px',
						width: '50%',
						marginTop: '5%',
						background: '#3c324e',
					}}
					onClick={() => submitProblem()}
					variant="contained"
				>
					ამოხსნის გაგზავნა
				</Button>

                
			</Box>
                
        </Box>
    );
    
    

};