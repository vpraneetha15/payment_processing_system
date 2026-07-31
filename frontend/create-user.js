const storageKey = "pps_users";

const form = document.getElementById("createUserForm");
const clearBtn = document.getElementById("clearBtn");
const clearUsersBtn = document.getElementById("clearUsersBtn");
const userList = document.getElementById("userList");
const toast = document.getElementById("toast");

const fieldIds = [
  "fullName",
  "email",
  "employeeId",
  "role",
  "team",
  "preferredCurrency",
  "terms"
];

const previewMap = {
  fullName: document.getElementById("previewCard").querySelector(".preview-name"),
  role: document.getElementById("previewRole"),
  team: document.getElementById("previewTeam"),
  preferredCurrency: document.getElementById("previewCurrency"),
  isActive: document.getElementById("previewStatus")
};

function getUsers() {
  try {
    const raw = localStorage.getItem(storageKey);
    return raw ? JSON.parse(raw) : [];
  } catch (_error) {
    return [];
  }
}

function saveUsers(users) {
  localStorage.setItem(storageKey, JSON.stringify(users));
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.add("show");
  window.setTimeout(() => {
    toast.classList.remove("show");
  }, 1800);
}

function setError(id, message) {
  const el = document.getElementById(`${id}Error`);
  if (el) {
    el.textContent = message;
  }
}

function clearErrors() {
  fieldIds.forEach((id) => setError(id, ""));
}

function validate(formData) {
  let isValid = true;

  if (!formData.fullName || formData.fullName.trim().length < 3) {
    setError("fullName", "Enter at least 3 characters.");
    isValid = false;
  }

  if (!formData.email || !/^\S+@\S+\.\S+$/.test(formData.email)) {
    setError("email", "Enter a valid email address.");
    isValid = false;
  }

  if (!formData.employeeId || formData.employeeId.trim().length < 4) {
    setError("employeeId", "Employee ID must be at least 4 characters.");
    isValid = false;
  }

  if (!formData.role) {
    setError("role", "Select a role.");
    isValid = false;
  }

  if (!formData.team || formData.team.trim().length < 2) {
    setError("team", "Enter team name.");
    isValid = false;
  }

  if (!formData.preferredCurrency) {
    setError("preferredCurrency", "Select preferred currency.");
    isValid = false;
  }

  if (!formData.terms) {
    setError("terms", "Please confirm details before creating user.");
    isValid = false;
  }

  const users = getUsers();
  const duplicateId = users.find(
    (user) => user.employeeId.toLowerCase() === formData.employeeId.toLowerCase()
  );

  if (duplicateId) {
    setError("employeeId", "Employee ID already exists.");
    isValid = false;
  }

  const duplicateEmail = users.find(
    (user) => user.email.toLowerCase() === formData.email.toLowerCase()
  );

  if (duplicateEmail) {
    setError("email", "Email is already registered.");
    isValid = false;
  }

  return isValid;
}

function formToObject() {
  return {
    fullName: document.getElementById("fullName").value.trim(),
    email: document.getElementById("email").value.trim(),
    employeeId: document.getElementById("employeeId").value.trim(),
    role: document.getElementById("role").value,
    team: document.getElementById("team").value.trim(),
    preferredCurrency: document.getElementById("preferredCurrency").value,
    notes: document.getElementById("notes").value.trim(),
    isActive: document.getElementById("isActive").checked,
    wantsAlerts: document.getElementById("wantsAlerts").checked,
    terms: document.getElementById("terms").checked,
    createdAt: new Date().toISOString()
  };
}

function refreshPreview() {
  const fullName = document.getElementById("fullName").value.trim();
  const role = document.getElementById("role").value;
  const team = document.getElementById("team").value.trim();
  const preferredCurrency = document.getElementById("preferredCurrency").value;
  const isActive = document.getElementById("isActive").checked;

  previewMap.fullName.textContent = fullName || "New User";
  previewMap.role.textContent = role || "-";
  previewMap.team.textContent = team || "-";
  previewMap.preferredCurrency.textContent = preferredCurrency || "-";
  previewMap.isActive.textContent = isActive ? "Active" : "Inactive";
}

function renderUserList() {
  const users = getUsers();

  if (!users.length) {
    userList.innerHTML = "<li>No users added yet.</li>";
    return;
  }

  const latest = [...users].reverse().slice(0, 5);

  userList.innerHTML = latest
    .map((user) => {
      const badgeClass = user.isActive ? "badge" : "badge inactive";
      const badgeText = user.isActive ? "Active" : "Inactive";
      return `
        <li>
          <div class="line-1">
            <span>${escapeHtml(user.fullName)}</span>
            <span class="${badgeClass}">${badgeText}</span>
          </div>
          <div class="line-2">${escapeHtml(user.role)} | ${escapeHtml(user.team)} | ${escapeHtml(user.preferredCurrency)}</div>
        </li>
      `;
    })
    .join("");
}

function escapeHtml(text) {
  const map = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;"
  };

  return String(text).replace(/[&<>"']/g, (char) => map[char]);
}

function resetForm() {
  form.reset();
  document.getElementById("isActive").checked = true;
  document.getElementById("wantsAlerts").checked = true;
  clearErrors();
  refreshPreview();
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  clearErrors();

  const payload = formToObject();
  if (!validate(payload)) {
    showToast("Please fix form errors.");
    return;
  }

  const users = getUsers();
  users.push(payload);
  saveUsers(users);

  showToast("User created successfully.");
  renderUserList();
  resetForm();
});

clearBtn.addEventListener("click", () => {
  resetForm();
  showToast("Form cleared.");
});

clearUsersBtn.addEventListener("click", () => {
  localStorage.removeItem(storageKey);
  renderUserList();
  showToast("Saved users removed.");
});

["input", "change"].forEach((eventName) => {
  form.addEventListener(eventName, refreshPreview);
});

renderUserList();
refreshPreview();
