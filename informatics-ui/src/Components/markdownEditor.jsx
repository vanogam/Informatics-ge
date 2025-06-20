import React, {useContext, useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";
import getMessage from "./lang";
import {AxiosContext} from '../utils/axiosInstance'
import {Button} from "@mui/material";

const MarkdownEditor = ({value, onChange, loadEndpoint, saveText, imageDownloadFunc, imageUploadAddress, submitFunc}) => {
    const [activeTab, setActiveTab] = useState("editor");
    const axiosInstance = useContext(AxiosContext)

    const handleInputChange = (e) => {
        onChange(e.target.value);
    };

    const loadData = () => {
        axiosInstance.get(loadEndpoint)
            .then(response => {
                if (response.status === 200) {
                    onChange(response.data.statement || "");
                }
            })
    }

    useEffect(() => {
        loadEndpoint && loadData()
    }, [])

    const handlePaste = async (e) => {
        const items = e.clipboardData.items;
        for (const item of items) {
            if (item.type.startsWith("image/")) {
                const file = item.getAsFile();
                const formData = new FormData();
                formData.append("file", file);

                axiosInstance.post(imageUploadAddress,
                    formData,
                ).then((response) => {
                    const imageUrl = response.data.imageUrl;

                    if (response.status === 200) {
                        const imageMarkdown = `![Image](${imageUrl})\n`;
                        onChange((prev) => prev + imageMarkdown);
                    } else {
                        console.error("Image upload failed:", response.statusText);
                    }
                });
                e.preventDefault();
            }
        }
    };

    return (
        <div>
            <div style={{ display: "flex", borderBottom: "1px solid #ccc" }}>
                <button
                    style={{
                        flex: 1,
                        padding: "10px",
                        cursor: "pointer",
                        background: activeTab === "editor" ? "#f0f0f0" : "white",
                        border: "none",
                        borderBottom: activeTab === "editor" ? "2px solid blue" : "none",
                    }}
                    onClick={() => setActiveTab("editor")}
                >
                    {getMessage('ka', 'editor')}
                </button>
                <button
                    style={{
                        flex: 1,
                        padding: "10px",
                        cursor: "pointer",
                        background: activeTab === "preview" ? "#f0f0f0" : "white",
                        border: "none",
                        borderBottom: activeTab === "preview" ? "2px solid blue" : "none",
                    }}
                    onClick={() => setActiveTab("preview")}
                >
                    {getMessage('ka', 'statement')}
                </button>
            </div>
            <div style={{ padding: "10px" }}>
                {activeTab === "editor" && (
                    <textarea
                        style={{ width: "100%", height: "200px", fontSize: "16px" }}
                        placeholder={getMessage('ka', 'markdownPlaceholder')}
                        value={value}
                        onChange={handleInputChange}
                        onPaste={handlePaste}
                    />
                )}
                {activeTab === "preview" && (
                    <div style={{ border: "1px solid #ccc", padding: "10px" }}>
                        <ReactMarkdown
                            children={value}
                            remarkPlugins={[remarkMath]}
                            rehypePlugins={[rehypeMathjax]}
                            urlTransform={imageDownloadFunc}
                        />
                    </div>
                )}
            </div>
            <div style={{marginLeft: 'auto'}}>
                <Button
                    variant="contained"
                    color="secondary"
                    sx={{ backgroundColor: '#2f2d47' }}
                    disabled={!value || value.trim() === ""}
                    onClick={
                        () => {
                            submitFunc(value)
                        }
                    }
                >
                    {saveText || getMessage('ka', 'save')}
                </Button>
            </div>
        </div>
    );
};

export default MarkdownEditor;