import { useState } from "react";
import Editor from "react-simple-code-editor";
import { highlight, languages } from "prismjs/components/prism-core";
import axios from "axios";

import Box from "@mui/material/Box";
import Modal from "@mui/material/Modal";
import Table from "@mui/material/Table";
import Paper from "@mui/material/Paper";
import TableRow from "@mui/material/TableRow";
import TableHead from "@mui/material/TableHead";
import TableCell from "@mui/material/TableCell";
import TableBody from "@mui/material/TableBody";
import Typography from "@mui/material/Typography";
import TableContainer from "@mui/material/TableContainer";
import { useParams } from "react-router-dom";
import { useEffect } from "react";
export default function Submissions() {
  const [submissions, setSubmissions] = useState([]);
  const [selectedSubmission, setSelectedSubmission] = useState({});
  const [popUp, setPopUp] = useState(false);
  const { contest_id } = useParams();

  const hightlightWithLineNumbers = (input, grammar, language) =>
    highlight(input, grammar, language)
      .split("\n")
      .map((line, i) => `${line}`)
      .join("\n");
      useEffect(() => {
        axios
          .get(`http://localhost:8080/contest/${contest_id}/submissions`)
          .then((response) => {
            if (response.data.status === "SUCCESS")
              setSubmissions(response.data.submissions);
            else return <>NO SUBMISSIONS FOUND</>;
          });
      }, []);
  return (
    <>
      <Typography 	sx={{ color: '#452c54', fontWeight: 'bold' }} align="center" variant="h6" mb="1rem" mt="1rem">
        ჩემი მცდელობები
      </Typography>
      <TableContainer
        component={Paper}
        sx={{ maxWidth: "80%", marginInline: "auto" }}
      >
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>Contest | Task</TableCell>
              <TableCell align="right">Username</TableCell>
              <TableCell align="right">Submission Time</TableCell>
              <TableCell align="right">Language</TableCell>
              <TableCell align="right">Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {submissions.map((submission) => (
              <TableRow
                onClick={() => {
                  setSelectedSubmission(submission);
                  setPopUp(true);
                }}
                key={submission.id}
                sx={{
                  "&:last-child td, &:last-child th": { border: 0 },
                  cursor: "pointer",
                  "&:hover": { backgroundColor: "#eee" },
                }}
              >
                <TableCell component="th" scope="row">
                  {submission.contestId} | {submission.taskId}
                </TableCell>
                <TableCell align="right">{submission.username}</TableCell>
                <TableCell align="right">{submission.submissionTime}</TableCell>
                <TableCell align="right">{submission.language}</TableCell>
                <TableCell align="right">
                  {submission.status === "FINISHED"
                    ? submission.status +" |Score: " +submission.score
                    : submission.statuss}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <Modal open={popUp} onClose={() => setPopUp(false)}>
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            maxHeight: "80%",
            overflowY: "auto",
            width: "600px",
            bgcolor: "white",
            border: `2px solid ;`,
            borderRadius: "0.5rem",
            boxShadow: 24,
            p: 4,
          }}
        >
          <Paper elevation={4} sx={{ padding: "1rem", marginBottom: "1rem" }}>
            <Typography sx={{ fontSize: "10px", fontWeight: "400" }}>
              User: {selectedSubmission.username}
            </Typography>
            <Typography sx={{ fontSize: "10px", fontWeight: "400" }}>
              Lang: {selectedSubmission.language}
            </Typography>
            <Typography sx={{ fontSize: "10px", fontWeight: "400" }}>
              Submission Time: {selectedSubmission.submissionTime}
            </Typography>
          </Paper>
          <Paper elevation={4} sx={{ padding: "1rem", marginBottom: "1rem" }}>
            <Editor
              value={selectedSubmission.text}
              highlight={(code) =>
                hightlightWithLineNumbers(code, languages.cpp, "cpp")
              }
              textareaId="codeArea"
              style={{
                overflowY: "auto",
                maxHeight: "20rem",
                overflowY: "auto",
                fontFamily: '"Fira code", "Fira Mono", monospace',
                fontSize: 12,
              }}
            />
          </Paper>
          <Paper elevation={4} sx={{ padding: "1rem", marginBottom: "1rem" }}>
            <Typography sx={{ fontSize: "13px", fontWeight: "400" }}>
              Compilation Result: {selectedSubmission.compilationResult}
              <br />
              Compilation Message: {selectedSubmission.compilationMessage}
            </Typography>
          </Paper>
          {selectedSubmission.compilationResult === "ok" ? (
            selectedSubmission.status === "FINISHED" ? (
              <Paper elevation={4} sx={{ padding: "1rem" }}>
                <Typography align="center" variant="h6" mb="1rem">
                  Test cases
                </Typography>
                {selectedSubmission?.results?.map((testCase) => (
                  <Paper elevation={4} sx={{ padding: "1rem" , marginBottom: "1rem",
                    backgroundColor:
                      testCase.outcome === "Correct" ? "#CFE8D3" : "#E8CFD4",
                  }}>
                    <Typography sx={{fontSize: "15px"}}>#{testCase.idx}</Typography>
                    <Box sx={{display: "flex", justifyContent: "space-between"}}>
                        <Typography sx={{fontSize: "15px"}} >
                          Status:{" "}
                          <span style={{ fontWeight: 700 }}>
                            {testCase.outcome}
                          </span>
                        </Typography>
                        <Typography sx={{fontSize: "15px"}}>
                          Message:{" "}
                          <span style={{ fontWeight: 700 }}>{testCase.text}</span>
                        </Typography>
                        <Typography sx={{fontSize: "15px"}}>
                          Time:{" "}
                          <span style={{ fontWeight: 700 }}>{testCase.time}</span>
                        </Typography>
                        <Typography sx={{fontSize: "15px"}}>
                          Memory:{" "}
                          <span style={{ fontWeight: 700 }}>{parseInt(testCase.memory/1000) + "KB"}</span>
                        </Typography>
                    </Box>
                  </Paper>
                ))}
              </Paper>
            ) : (
              <Paper elevation={4} sx={{ padding: "1rem" }}>
                <Typography>
                  Running on test: {selectedSubmission.currentTest}
                </Typography>
              </Paper>
            )
          ) : (
            <Paper elevation={4} sx={{ padding: "1rem" }}>
              <Typography>Error Compiling</Typography>
            </Paper>
          )}
        </Box>
      </Modal>
    </>
  );
}
