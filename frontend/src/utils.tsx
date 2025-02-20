// Helper function ensures that inputted email is a valid email
export function validateEmail(inputEmail: string){
  const emailRegex = new RegExp(/^[A-Za-z0-9_!#$%&'*+/=?`{|}~^.-]+@[A-Za-z0-9.-]+$/, 'gm');

  return emailRegex.test(inputEmail);
}

const colorPalette = ["red", "blue", "green", "yellow", "purple", "orange"]
export function pickPalette(name: string) {
  const index = name.charCodeAt(0) % colorPalette.length;
  return colorPalette[index];
}