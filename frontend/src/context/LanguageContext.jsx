import React, { createContext, useState, useContext } from 'react';
const LanguageContext = React.createContext({
    language: 'vi', // Ngôn ngữ mặc định
    setLanguage: () => { }, // Một hàm rỗng để không bị lỗi khi gọi
});

export const LanguageProvider = ({ children }) => {
    const [language, setLanguage] = useState('vi');
    const value = { language, setLanguage };
    return (
        <LanguageContext.Provider value={value}>
            {children}
        </LanguageContext.Provider>
    )
}
// hook
export const useLanguage = () => {
    return useContext(LanguageContext);

}