import axios from "axios"
import { useState, useEffect } from "react"
export default function ContestRegisterPopUp({contestId}){
    const [text, setText] = useState("")
    // useEffect(() =>{ axios
	// 		.get(`http://localhost:8080/contest/${contestId}/is-registered`, {
	// 		})
	// 		.then((response) =>{
    //             registered = response.data.registered
    //             if (registered){
    //                 setText("კონტესზტე უკვე დარეგისტრირებული ხარ")
    //             }else{
    //                 axios
    //                 .post(`http://localhost:8080/contest/${contestId}/register`, {
    //                 })
    //                 .then((response) => {
    //                     setText("წარმატებით დარეგისტრირდი")})
    //             }
    //         }), []}

    useEffect(() => {

                    axios
                    .post(`http://localhost:8080/contest/${contestId}/register`, {
                    })
                    .then((response) => {
                        if(response.status == "SUCCESS"){
                            setText("  წარმატებით დარეგისტრირდი! 🎉")
                        }else{
                            setText("🛑 შეცდომა რეგისტრაციისას ")
                        }
                        })

			.catch((error) => console.log(error))
	}, [])
    return (<>{text}</>)

}