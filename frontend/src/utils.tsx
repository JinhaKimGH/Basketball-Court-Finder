// Helper function ensures that inputted email is a valid email
export function validateEmail(inputEmail: string){
  const emailRegex = new RegExp(/^[A-Za-z0-9_!#$%&'*+/=?`{|}~^.-]+@[A-Za-z0-9.-]+$/, 'gm');

  return emailRegex.test(inputEmail);
}