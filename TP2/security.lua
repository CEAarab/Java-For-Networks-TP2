banned_word = { "kill", "hate", "cea" }

function banned_words(message)
    for _, word in ipairs(banned_word) do
        if string.find(message:lower(), word) then
            return true
        end
    end
    return false
end

function can_change_to_admin(user_ip)
    if user_ip == "127.0.0.1" or user_ip == "::1" then
        return true
    else
        return false
    end
end
function can_execute_command(user_nickname, command)
    if command == "ban" then
        if user_nickname == "admin" then
            return true
        else
            return false
        end
    else
        return true
    end
end