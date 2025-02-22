import { useContext, useState } from "react";
import { LuCheck, LuPencil } from "react-icons/lu";
import { Button, DataList, Flex, Text } from "@chakra-ui/react";
import { custom_input as CInput } from "./ui/custom_input";
import { validateEmail } from "@/utils";
import { AuthState } from "@/context/AuthContext";
import { AuthContext } from "@/context/AuthContext";

// Define types
interface EditableField {
  value: string;
  editing: boolean;
  errorMessage: string;
}

interface UserProfileState {
  displayName: EditableField;
  email: EditableField;
  password: EditableField;
}

interface ProfileEditProps {
  displayName: string;
  email: string;
}

export default function ProfileEdit({ displayName, email }: ProfileEditProps) {

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }

  const { setAuthState } = authContext;

  // Form field states
  const [isLoading, setIsLoading] = useState(false);
  const [editFields, setEditFields] = useState<UserProfileState>({
    displayName: { value: displayName, editing: false, errorMessage: "" },
    email: { value: email, editing: false, errorMessage: "" },
    password: { value: "", editing: false, errorMessage: "" },
  });

  const toggleEdit = (field: keyof UserProfileState) => {
    setEditFields((prev) => ({
      ...prev,
      [field]: { ...prev[field], editing: !prev[field].editing },
    }));
  };

  const handleFieldChange = (field: keyof UserProfileState, newValue: string) => {
    setEditFields((prev) => ({
      ...prev,
      [field]: { ...prev[field], value: newValue },
    }));
  };

  const handleSubmit = (field: keyof UserProfileState) => {
    let errorMessage = "";
    setIsLoading(true);

    if (field === "email" && !validateEmail(editFields.email.value)) {
      errorMessage = 'Email must be valid.';
      setIsLoading(false);
    }

    if (field === "displayName") {
      if (editFields.displayName.value.trim().length < 3 || 
        editFields.displayName.value.length > 20) {
        errorMessage = 'Display name must be between 3 and 20 characters.';
      }
      if (!/^[a-zA-Z0-9 ]+$/.test(editFields.displayName.value)) {
        errorMessage = 'Display name can only contain letters, numbers, and spaces.';
      }
    }

    if (field === "password") {
      if (editFields.password.value.length < 8) {
        errorMessage = 'Password must be at least 8 characters long.';
      }
      if (!/[A-Z]/.test(editFields.password.value) || 
        !/[a-z]/.test(editFields.password.value) || 
        !/[0-9]/.test(editFields.password.value) || 
        !/[\W_]/.test(editFields.password.value)) {
        errorMessage = 'Password must include uppercase, lowercase, number, and special character.';
      }
    }
    
    setEditFields((prev) => ({
      ...prev,
      [field]: { ...prev[field], errorMessage: errorMessage },
    }));
    if(errorMessage) {
      setIsLoading(false);
      return;
    }

    // API Call for updating user profile
    fetch(`${baseApiUrl}/api/users/${field}`, {
      method: "PUT",
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        [field]: editFields[field].value,
      }),
      credentials: 'include',
    }).then((res) => {
      if (res.ok) {
        // Successful update -> do something
        setIsLoading(false);
        toggleEdit(field);
        if (field !== "password") {
          setAuthState((prev : AuthState) : AuthState => ({
            ...prev,
            user: prev.user ? { ...prev.user, [field]: editFields[field].value } : null,
          }));
        }
        return;
      }
      return res.text().then(text => {
        setEditFields((prev) => ({
          ...prev,
          [field]: { ...prev[field], errorMessage: errorMessage },
        }));
        setIsLoading(false);
        toggleEdit(field);
        throw new Error(`HTTP error! status: ${res.status}, message: ${text}`);
      });
    })
    .catch((error) => {
      console.error('Error updating profile:', error);
      //TODO: REPLACE WITH LOGGING LATER
    })
  };

  return (
    <DataList.Root size="lg">
      {(["displayName", "email", "password"] as (keyof UserProfileState)[]).map((field) => (
        <DataList.Item key={field} justifyContent={"center"} padding={2}>
          <DataList.ItemLabel>
            <Flex
              justifyContent={"space-around"}
              align={"center"}
              gap="8"
              width="100%"
            >
                <CInput
                  name={field}
                  type={field === "password" ? "password" : "text"}
                  value={editFields[field].value}
                  onChange={(e: { target: { value: string; }; }) => handleFieldChange(field, e.target.value)}
                  placeholder={field === "displayName" ? displayName : field === "email" ? email : ""}
                  label={field === "displayName" ? "Username" : field === "email" ? "Email" : "Password"}
                  invalid={editFields[field].errorMessage !== ""}
                  disabled={!editFields[field].editing}
                  required
                />
              {
                !editFields[field].editing ?
                <Button variant="plain" onClick={() => toggleEdit(field)}>
                    <LuPencil/>
                  </Button>
                : 
                <Button 
                variant="plain" 
                loading={isLoading} 
                onClick={() => handleSubmit(field)}
                >
                    <LuCheck/>
                </Button>
              }
            </Flex>
          </DataList.ItemLabel>
          <Text textStyle="xs" color={"#ef4444"}>{editFields[field].errorMessage}</Text>
        </DataList.Item>
      ))}
    </DataList.Root>
  );
}
